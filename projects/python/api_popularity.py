import numpy as np
import pandas as pd
import sys
import random
from scipy.sparse import csr_matrix
from scipy.stats import pearsonr

_root = "/Users/lewis/Documents/runtime/voicepersonality/survey/"
_root = "/home/wsuser/projects/python/"
_format = "{\"songs\":\"_sids_\"}"
_file_rating = _root + "20220824_D3.csv"
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
    subject_ratings = eval(sys.argv[1:][0])

    # df_ratings = pd.read_csv(_file_rating)
    # predicted_ratings = []
    # for song_id in _200_songs:
    #     avg_rating = df_ratings.loc[df_ratings['sid'] == int(song_id)].rating.mean()
    #     predicted_ratings.append([song_id, avg_rating])
    # predicted_ratings.sort(reverse=True, key=lambda x: x[1])
    # recommended_song_ids = [predicted_ratings[i][0] for i in range(_num_recommended)]
    # print(recommended_song_ids)

    # mock a subject ratings begin - remove after web service call is ready
    # ratings = ["1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5"]
    # song_ids = _200_songs[random.sample(range(0, 200), 20)]
    # subject_ratings = {song_id: ratings[random.randrange(0, 9)] for song_id in song_ids}
    # subject_uid = '7d6124f6-3012-4115-9f37-64e3d9c6531b'
    # mock a subject's rating end


    # recommended_song_ids = ['111', '382', '466', '083', '488', '453', '084', '448', '428', '271']
    recommended_song_ids = ['111', '382', '466', '083', '488', '453', '084', '448', '428', '271', '462', '438', '427', '443', '505', '410',
     '409', '398', '499', '464', '270', '110', '445', '455', '249', '452', '498', '021', '340', '082']
    rated_ids = list(subject_ratings.keys())
    count = 0
    ret = []
    for id in recommended_song_ids:
        if id not in rated_ids:
            ret.append(id)
            count = count + 1
            if count == 10:
                break
    print(_format.replace("_sids_", ";".join(ret)))