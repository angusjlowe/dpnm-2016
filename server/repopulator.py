import pyrebase

#define firebase connection parameters
config = {
	"apiKey": "07318b5d9391bc98755cd3a72f5b9b9c590930a4",
	"authDomain": "studentstudyspaces.firebaseapp.com",
	"databaseURL": "https://studentstudyspaces.firebaseio.com/",
	"storageBucket": "studentstudyspaces.appspot.com",
	"serviceAccount": "/home/joe/postech_research/server_store/serviceAccountCredentials.json"
	}

#initialise connection with firebase
firebase = pyrebase.initialize_app(config)
db = firebase.database()

#create database object of study spaces
res = db.child('study spaces').get()
data = {
	  "content" : "The other comment is lying",
      "date" : "07/07/2016/14:45",
      "votes" : "0"
}
spaces = res.each()
for i in spaces:
	db.child('study spaces').child(i.key()).child('comments').push(data)