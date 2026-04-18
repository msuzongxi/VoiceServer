import random
import numpy as np
import pandas as pd

# _root = "/Users/lewis/Documents/runtime/voicepersonality/survey/"
_root = "/home/wsuser/projects/python/"

_file_voice_emb = _root + "20220901_D1.csv"
_file_rating = _root + "20220824_D3.csv"
_k = 10
_num_recommended = 10
_200_songs = np.array(
        ['475', '503', '463', '493', '505', '465', '482', '476', '490', '466', '499', '467', '469', '481', '464', '334',
         '473', '468', '489', '462', '214', '326', '506', '028', '019', '047', '124', '080', '203', '217', '039', '049',
         '270', '266', '204', '155', '141', '046', '067', '200', '507', '492', '497', '111', '500', '496', '504', '130',
         '480', '470', '371', '510', '509', '380', '110', '491', '495', '009', '498', '501', '451', '445', '460', '444',
         '459', '452', '453', '446', '458', '021', '457', '340', '447', '455', '449', '335', '443', '456', '450', '448',
         '238', '224', '235', '234', '225', '230', '221', '319', '308', '223', '228', '072', '031', '227', '316', '240',
         '229', '315', '237', '233', '370', '088', '261', '185', '205', '113', '084', '202', '212', '253', '100', '109',
         '044', '091', '118', '488', '086', '023', '220', '101', '419', '410', '408', '409', '068', '418', '414', '404',
         '405', '420', '406', '401', '411', '415', '407', '069', '034', '338', '403', '413', '149', '150', '170', '356',
         '358', '174', '271', '180', '177', '161', '164', '344', '360', '165', '167', '179', '343', '175', '345', '353',
         '423', '074', '082', '428', '434', '288', '438', '250', '427', '242', '249', '369', '336', '436', '296', '073',
         '075', '426', '083', '244', '395', '264', '391', '387', '374', '385', '104', '246', '484', '392', '382', '397',
         '394', '389', '393', '022', '388', '400',
         '416', '398'])


if __name__== "__main__":

    # 1. calculate voice similarity matrix
    df_voice_emb = pd.read_csv(_file_voice_emb)
    uids = df_voice_emb['uid'].values
    uembs = df_voice_emb[["vd"+str(i) for i in range(1, 257)]].values

    # 2. for each subject, predict her song ratings
    df_ratings = pd.read_csv(_file_rating)
    ret = []
    for idx, row in df_ratings.iterrows():
        sid = row['sid']
        rating = row['rating']
        uid = row['uid']
        df_sid_ratings = df_ratings[df_ratings["sid"] == sid]
        df_sid_ratings2 = df_sid_ratings[df_sid_ratings["uid"] != uid]
        rated_uids = df_sid_ratings2['uid'].values
        target = uembs[uids == uid]

        other_mask = [np.isin(u, rated_uids) for u in uids]
        others = uembs[other_mask]
        sims = np.dot(others, target.T)
        df_sims = pd.DataFrame([[rated_uids[i], sid, sims[i]] for i in range(sims.shape[0])], columns=["uid", "sid", "similarity"])
        df_sims_ratings = pd.merge(df_sid_ratings2, df_sims, on=["uid", "sid"])
        df_sims_ratings.sort_values(by=["similarity"], ascending=False, inplace=True)
        predicted_rating = np.average(df_sims_ratings.iloc[0:_k, :].rating.values,
                                      weights=df_sims_ratings.iloc[0:_k, :].similarity.values)[0]
        print(uid, sid, rating, predicted_rating)
        ret.append([uid, sid, rating, predicted_rating])

    # 4. mse evaluation
    df_ret = pd.DataFrame(ret, columns=["uid", "sid", "rating", "predicted_rating"])
