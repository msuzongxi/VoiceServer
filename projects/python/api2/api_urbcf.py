import random
import numpy as np
import pandas as pd
from scipy.sparse import csr_matrix
from scipy.stats import pearsonr
import sys
import warnings
warnings.filterwarnings('ignore')

#####_root = "/Users/lewis/Documents/runtime/voicepersonality/0227/"
_root = "/home/azureuser/projects/python/"
_file_rating = _root + "20230227_D3.csv"

_format = "{\"songs\":\"_sids_\"}"
#replace k value
_k = 60
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
         '394', '389', '393', '022', '388', '400', '416', '398'])

def pearson(a, b):
    a = np.array(a)
    b = np.array(b)
    common = ~np.logical_or(np.isnan(a), np.isnan(b))
    if sum(common) < 2:
        return 0.
    psim = pearsonr(a[common], b[common])[0]
    if np.isnan(psim):
        return 0.
    return psim

if __name__== "__main__":
    # 1. read subject ratings and subject id from web service call
    subject_ratings = eval(sys.argv[1:][0])
    # print(subject_ratings.keys())
    subject_uid = sys.argv[1:][1]

    # mock a subject ratings begin - remove after web service call is ready
    # ratings = ["1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5"]
    # song_ids = _200_songs[random.sample(range(0, 200), 20)]
    # subject_ratings = {song_id: ratings[random.randrange(0, 9)] for song_id in song_ids}
    # subject_uid = '7d6124f6-3012-4115-9f37-64e3d9c6531b'
    # mock a subject's rating end

    # 2. calculate rating similarity between the new subject and other 199 subjects
    df_subject_ratings = pd.DataFrame([[subject_uid, song_id, float(rating)] for song_id, rating in subject_ratings.items()],
                                      columns=["uid", "sid", "rating"])
    subject_rating = [df_subject_ratings.loc[df_subject_ratings['sid'] == s]['rating'].values[0] if s in set(df_subject_ratings['sid']) else 0 for s in _200_songs]
    df_ratings = pd.read_csv(_file_rating)
    vector_ratings = []
    for u in set(df_ratings['uid']):
      u_ratings = df_ratings.loc[df_ratings['uid'] == u]
      u_rating = [float(u_ratings.loc[u_ratings['sid'] == int(s)]['rating'].values[0]) if int(s) in set(u_ratings['sid']) else 0 for s in _200_songs]
      u_rating = [u] + u_rating
      vector_ratings.append(u_rating)

    total_subject_similarity = np.array([pearson(vec[1:], subject_rating) for vec in vector_ratings])
    total_subject_ids = np.array([vec[0] for vec in vector_ratings])
    df_similarity = pd.DataFrame(np.vstack((total_subject_ids, total_subject_similarity)).transpose(),
                                 columns=["uid", "similarity"])
    df_similarity['similarity'] = df_similarity['similarity'].astype(float)
    df_knn = df_similarity.sort_values(by=['similarity'], ascending=False).iloc[:_k, ] ####Yaqiong: find KNN before rating calculation

    # 3. predict 200 song ratings
    tbp_songs = np.setdiff1d(_200_songs, list(subject_ratings.keys())) #excluding rated songs
    df_ratings = pd.read_csv(_file_rating)
    predicted_ratings = []
    for song_id in tbp_songs:
        df_song_id_ratings = df_ratings.loc[df_ratings['sid'] == int(song_id)]
        df_knn_ratings = pd.merge(df_song_id_ratings, df_knn, on=['uid'], how='inner')  ####Yaqiong: find KNN before rating calculation
        if df_knn_ratings['similarity'].sum() != 0: ####Yaqiong: to avoid potential 0 division
              df_knn_ratings['weighted_rating'] =  df_knn_ratings['rating'] * df_knn_ratings['similarity']/df_knn_ratings['similarity'].sum()
              song_rating = df_knn_ratings['weighted_rating'].sum()
        else:
              song_rating = 0
        predicted_ratings.append([song_id, song_rating])


    # 4. recommend top 10 based on user-based collaborative filtering
    predicted_ratings.sort(reverse=True, key=lambda x: x[1])
    recommended_song_ids = [predicted_ratings[i][0] for i in range(_num_recommended)]
    print(_format.replace("_sids_", ";".join(recommended_song_ids)))
    # print(predicted_ratings)
