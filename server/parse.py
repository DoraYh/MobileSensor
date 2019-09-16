# def get_contends(path):
#     with open(path) as file_object:
#         contends = file_object.read()
#     return contends

def get_contends_arr(contends):
    contends_arr = []
    contends_arr = str(contends).split('\n')
    
    count = 0
    while(contends_arr[count] != ""):
        count += 1
    print(count)
    
    date_arr = []
    location_arr = []
    ac_arr = []
    for i in range(count):
        date_loc_ac = []
        date_loc_ac = contends_arr[i].split('/')
        #print(len(date_loc_ac))
        date_arr.append(date_loc_ac[0])
        location_arr.append(date_loc_ac[1])
        ac_arr.append(date_loc_ac[2])
    
    return date_arr, location_arr, ac_arr

# if __name__ == "__main__":
#     path = "user_data/DataCollectUtils_1568539450153.txt"
#     contends = get_contends(path)
#     date_arr, location_arr, ac_arr = get_contends_arr(contends)

#     for i in range(len(date_arr)):
#         ac_arr_index = ac_arr[i].strip().split(';')
 
#         date = date_arr[i]
#         location_x_y = location_arr[i].split(';')
#         latitude = float(location_x_y[0][10:])
#         longitude = float(location_x_y[1][11:])
#         print(len(ac_arr_index))
#         #space
#         for j in range(len(ac_arr_index)-1):
#             ac_x_y_z = ac_arr_index[j].split()
#             ac_x = float(ac_x_y_z[0][2:])
#             ac_y = float(ac_x_y_z[1][2:])
#             ac_z = float(ac_x_y_z[2][2:])
#             print(date + ' ' + str(latitude) + ' ' + str(longitude) +' '+ str(ac_x) + ' ' + str(ac_y) + ' ' + str(ac_z))
#             print('\n')