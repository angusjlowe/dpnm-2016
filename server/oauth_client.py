from oauth2client.service_account import ServiceAccountCredentials

scopes = [
	'https://www.googleapis.com/auth/firebase.database',
	'https://www.googleapis.com/auth/userinfo.email',
	'https://www.googleapis.com/auth/cloud-platform'
]
<<<<<<< HEAD
credentials = ServiceAccountCredentials.from_json_keyfile_name('auth_server.json', scopes=scopes)

def refresh_credentials():
	global scopes
	return ServiceAccountCredentials.from_json_keyfile_name('auth_server.json', scopes=scopes)
=======
credentials = ServiceAccountCredentials.from_json_keyfile_name('auth_server.json', scopes=scopes)
>>>>>>> 7127f656fb5222edc535723cdf5ea1dccbcc5e65
