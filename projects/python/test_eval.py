str_array = "[('noEnergy', 0.0, 0.6), ('music', 0.6, 5.52)]"
array = eval(str_array)
for (audio_type, start_time, end_time)  in array:
    print(audio_type, start_time, end_time)
