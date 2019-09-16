# python 2.7
# to run this server, use command 'python server.py 80' (on port 80)
import re
import web
import json
import time
from requests_toolbelt.multipart import decoder

MAGIC_CONNECTION_KEY = 'SYSU_SMC'

urls = (
	'/', 'index'
)

USER_DATA_DIR = "./user_data/"

class index:
	def POST(self):
		data = web.data()
		# 'multipart/form-data' requests have boundaries
		boundary = data[data.find(b'--') + 2: data.find(b'\r\n')]
		content_type = 'multipart/form-data; boundary=' + boundary
		d = decoder.MultipartDecoder(data, content_type)

		for part in d.parts:
			try:
				# parse one part
				content_disposition = {k:v.strip('"') for k,v in re.findall(r'(\S+)=(".*?"|\S+)', part.headers['Content-Disposition'])}
				filename = content_disposition['filename']

				print 'RECEIVE FILE FROM USER: ' + filename

				# files containing data collected by sensors have the same name
				# rename to store all files
				'''
				if filename == 'DataCollectUtils':
					filename += '_{}.txt'.format(int(time.time()))
				'''
				if filename.find('DataCollectUtils') != -1:
					filename += '.txt'
				with open(USER_DATA_DIR + filename, 'wb') as f:
					f.write(part.content)
			except Exception as e:
				print e

	def GET(self):
		try:
			if web.input()['magicKey'] == MAGIC_CONNECTION_KEY:
				print 'A device is connected!'
				return MAGIC_CONNECTION_KEY
			else:
				print web.input()['magicKey']
		except Exception as e:
			print(e.what())
		return 'Invalid Identification'

if __name__ == '__main__':
	web.config.debug = False
	app = web.application(urls, globals())
	app.run()