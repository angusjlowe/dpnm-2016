from firebase import firebase

firebase = firebase.FirebaseApplication('https://studentstudyspaces.firebaseio.com/')
res = firebase.get('/study spaces/', None)

for i in range(len(res)):
	if (res[i] is not None) and (res[i].get(u'comments') is not None):
		for j in range(len(res[i].get(u'comments'))):
			if res[i].get(u'comments')[j] is not None:
				if int(res[i].get(u'comments')[j].get(u'votes')) <= (-5):
					firebase.delete('/study spaces/'+str(i)+'/comments/'+str(j), None)