DIALECT = 'mysql'
DRIVER = 'pymysql'
USERNAME = 'root'
PASSWORD = '123456smc'
HOST = '192.168.1.92'
PORT = '3306'
DATABASE = 'test'
 
SQLALCHEMY_DATABASE_URI = '{}+{}://{}:{}@{}:{}/{}'.format(
    DIALECT,DRIVER,USERNAME,PASSWORD,HOST,PORT,DATABASE
)

SQLALCHEMY_COMMIT_ON_TEARDOWN = True
SQLALCHEMY_TRACK_MODIFICATIONS = True