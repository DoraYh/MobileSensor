import config
import parse
import pymysql
import os
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask import request

#init connection
app = Flask(__name__)
if config.USE_MYSQL:
    app.config.from_object(config)
    db = SQLAlchemy(app)

# test connection
MAGIC_CONNECTION_KEY = 'SYSU_SMC'
# the absolute path of mediatype file
MEDIATYPE_FILE_PATH = os.path.abspath('.') + "/user_data/"

if config.USE_MYSQL:
    #text
    class File(db.Model):
        __tablename__ = 'File'
        fid = db.Column(db.Integer, primary_key = True, autoincrement=True)
        name = db.Column(db.String(30),nullable=False)
        date = db.Column(db.String(50), nullable=False)
        latitude = db.Column(db.Float,nullable=False)
        longitude = db.Column(db.Float,nullable=False)
        accelerometer_x = db.Column(db.Float,nullable=False)
        accelerometer_y = db.Column(db.Float,nullable=False)
        accelerometer_z = db.Column(db.Float,nullable=False)


        def __init__(self, name, date, latitude, longitude, accelerometer_x, accelerometer_y, accelerometer_z):
            self.name = name
            self.date = date
            self.latitude = latitude
            self.longitude = longitude
            self.accelerometer_x = accelerometer_x
            self.accelerometer_y = accelerometer_y
            self.accelerometer_z = accelerometer_z
        
        def __repr__(self):
            return '<File %r>' % self.name

    #Mediatype
    class Media(db.Model):
        __tablename__ = 'Media'
        Mid = db.Column(db.Integer, primary_key = True, autoincrement=True)
        name = db.Column(db.String(50),nullable=False)
        Uri = db.Column(db.String(100),nullable=False)

        def __init__(self, name, Uri):
            self.name = name
            self.Uri = Uri
        
        def __repr__(self):
            return '<Media %r>' % self.name

    db.create_all()

@app.route('/', methods=['GET','POST'])
def upload():
    if request.method == 'POST':
        print(request.files)

        file = request.files['file']
        if file is None:
            return 'file is none!'
        
        if not config.USE_MYSQL:
            file_path = MEDIATYPE_FILE_PATH + file.filename
            file.save(file_path)
            return "upload success!"
        
        #mediatype file 
        if file.filename.find('DataCollectUtils') == -1:
            file_path = MEDIATYPE_FILE_PATH + file.filename
            file.save(file_path)
            
            m = Media(file.filename, file_path)
            db.session.add(m)

        #text file parse
        if file.filename.find('DataCollectUtils') != -1:
            f_data = file.read()
            date_arr, location_arr, ac_arr = parse.get_contends_arr(f_data)

            for i in range(len(date_arr)):
                ac_arr_index = ac_arr[i].strip().split(';')       
                date = date_arr[i]
                location_x_y = location_arr[i].split(';')
                latitude = float(location_x_y[0][10:])
                longitude = float(location_x_y[1][11:])

                #len(ac_arr_index)-1 -> space
                for j in range(len(ac_arr_index)-1):
                    ac_x_y_z = ac_arr_index[j].split()
                    ac_x = float(ac_x_y_z[0][2:])
                    ac_y = float(ac_x_y_z[1][2:])
                    ac_z = float(ac_x_y_z[2][2:])
                    f = File(file.filename, date, latitude, longitude, ac_x, ac_y, ac_z)
                    db.session.add(f)

        db.session.commit()
        return "upload success!"

    #test connection
    if request.method == 'GET':
        print(request.values)
        if request.values['magicKey'] == MAGIC_CONNECTION_KEY:
            print("A device is connected!")
            return MAGIC_CONNECTION_KEY

if __name__ == '__main__': 
    app.run(host='0.0.0.0', debug=True)