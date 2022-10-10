"""
# A ROUGH ALGO of what the script does-
    -connect to database
    -continually check for any update in database
    -if database is updated, fetch the updated data and download the video from the video link
    -once the video is downloaded, start capturing frames from the video and check whether there is any human face in the video
    -if face is found, mark it with a square and extract the face and save the face as a .jpg file
    -make a new video from the processed video frames
    -upload the processed video
"""

import os
import cv2
import time
import requests
import pyrebase
import numpy as np
import face_recognition

config = {
    "apiKey": "AIzaSyAYtUkE9wcGSrU9dSI1FU0AU2-czuRgFxs",
    "authDomain": "pythondbtest-de47f.firebaseapp.com",
    "databaseURL": "https://pythondbtest-de47f-default-rtdb.firebaseio.com",
    "projectId": "pythondbtest-de47f",
    "storageBucket": "pythondbtest-de47f.appspot.com",
    "messagingSenderId": "91516807715",
    "appId": "1:91516807715:web:925f598616b3614f071f96",
    "measurementId": "G-M3LSNQHDNC",
    "serviceAccount": "serviceAccountKey.json"
}

while 1:  # infinite loop trying to connect to db if connection isn't established
    try:
        firebase = pyrebase.initialize_app(config)
        database = firebase.database()  # connecting to db
        storage = firebase.storage()  # connecting to storage
        print(":::: CONNECTION TO ONLINE DATABASE ESTABLISHED ::::")
        break
    except:
        print(":::: CONNECTION TO ONLINE DATABASE FAILED ::::")
        time.sleep(10)

images = encodeList = []
path = 'res'  # path to store result
cascade_classifier = cv2.CascadeClassifier('haarcascades/haarcascade_frontalface_default.xml')
# haarcascade classifier to detect human front face

print("REAL-TIME DATA-MODIFICATON-LISTENER RUNNING...")
while 1:  # infinite while loop which checks for any change in db realtime
    changelist = []
    data = database.child("Users").get()
    for temp in data.each():  # checking for the Processed('Pro') attribute (whether it is 1, it means the video is
        # processed)
        dataval = database.child("Users").child(temp.key()).get()
        if dataval.val()["Pro"] != 1:  # if 'Pro' == 1 it means the video is already processed
            changelist.append(dataval.key())

    if len(changelist) != 0:  # changelist[] contains all the unprocessed data
        try:
            change = len(changelist)
            print("CHANGE DETECTED IN DATABASE - (", change, " new data )")
            time.sleep(1)
            change_i = 0

            # Here starts the  face detection, recognition and extraction from the video
            for tochange in changelist:  # trying to process newly added data
                print()
                print(':::: STARTING FACE DETECTION MODULE ::::')
                myList = os.listdir(path)  # listing elements of 'res' folder
                print("DELETIONG OLD FILES FROM TEMPORARY DATABASE...")
                for temp in myList:  # delete everythying in the 'res' folder
                    os.remove(path + '/' + temp)
                i = 1  # counter var used for naming cropped images of 'res' folder

                print("DOWNLOADING VIDEO FROM DATABASE...")
                # ---------------------------------------------------------------
                oldurl = database.child("Users").child(changelist[change_i]).child("Url").get().val()
                # storage.child("helpvideo.mp4").download("res/originalvideofire.mp4")
                # storage.child(oldurl).download("res/originalvideofire.mp4")
                vidurl = requests.get(oldurl, allow_redirects=True)
                open('res/originalvideofire.mp4', 'wb').write(vidurl.content)
                print("VIDEO DOWNLOADED AND STORED...")
                # ---------------------------------------------------------------

                # start capturing frames from video
                capture = cv2.VideoCapture("res/originalvideofire.mp4")
                frame_width = int(capture.get(cv2.CAP_PROP_FRAME_WIDTH))
                frame_height = int(capture.get(cv2.CAP_PROP_FRAME_HEIGHT))
                size = (frame_width, frame_height)
                fps = int(capture.get(cv2.CAP_PROP_FPS))
                result = cv2.VideoWriter('res/processedvideofire.avi', cv2.VideoWriter_fourcc(*'XVID'), fps, size)
                # making new video from processed frames
                print("PROCESSING VIDEO...")
                print("DETECTING AND RECOGNIZING FACES IN VIDEO...")
                while True:
                    ret, frame = capture.read()  # store the frame from the video
                    if ret == True:
                        gray = cv2.cvtColor(frame, 0)
                        detections = cascade_classifier.detectMultiScale(gray, 1.3,
                                                                         5)  # using cascade classifier to detect face
                        for (x, y, w, h) in detections:  # if face is detected, mark it
                            encfaces = []
                            faces = frame[y:y + h, x:x + w]  # extracting face
                            temp = cv2.cvtColor(faces, cv2.COLOR_BGR2RGB)
                            try:  # try...catch block to avoid 'IndexError', was causing problem while false-positive
                                # was encountered
                                encfaces = face_recognition.face_encodings(temp)[
                                    0]  # finding encodings of the currently encountered face
                                frame = cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0),
                                                      2)  # enclosing detected face in rectangle
                            except IndexError as e:
                                frame = cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255),
                                                      2)  # enclosing detected face in rectangle
                                continue

                            myList = os.listdir(path)  # listing elements of 'res' folder
                            if len(myList) == 0:  # if dir==empty, just store the face
                                cv2.imwrite('res/face-' + str(i) + '.jpg', faces)
                                print('NEW FACE ENCOUNTERED - SAVED')
                                i = i + 1
                            else:  # if dir!=empty, check whether the face already exists in 'res' folder
                                '''
                                'encodeList' contains list of encodings of all images in 'res' folder
                                'encfaces' contains the encoding of the present face
                                '''
                                faceDist = face_recognition.face_distance(encodeList,
                                                                          encfaces)  # calculates the distance b|w
                                # the current face and the already encoded faces
                                if len(encodeList) == 0:  # finds out the minimum of the distances calculated
                                    found = 1.0
                                else:
                                    found = np.min(faceDist)

                                if found >= 0.6:  # if face NOT FOUND, store the face
                                    cv2.imwrite('res/face-' + str(i) + '.jpg', faces)
                                    encodeList.append(encfaces)
                                    print('NEW FACE ENCOUNTERED -SAVED')
                                    i = i + 1
                        cv2.imshow('VidFeed:', frame)  # showing o/p (webcam feed)
                        result.write(frame)
                        if cv2.waitKey(1) & 0xFF == ord('q'):  # PRESS 'q' TO EXIT...
                            print(':::: PROCESSING TERMINATED FORCEFULLY ::::')
                            print()
                            break
                    else:
                        print("VIDEO PROCESSING FINISHED...")
                        break

                capture.release()  # stop capturing video
                result.release()  # stop making video
                cv2.destroyAllWindows()
                time.sleep(1)

                print("UPLOADING PROCESSED VIDEO TO DATABASE...")
                filename = changelist[change_i] + ".avi"
                storage.child(filename).put("res/processedvideofire.avi")
                newurl = storage.child(filename).get_url(None)
                database.child("Users").child(tochange).update({"ProLink": newurl, "Pro": 1})
                print("VIDEO UPLOADED SUCCESSFULLY...")
                print(':::: EXITTING PROGRAM ::::')
                print()
        except:
            print(":::: SOME ERROR ENCOUNTERED ::::")

        change_i = change_i + 1

    else:
        print("NO CHANGE DETECTED IN DATABASE")
    time.sleep(10)
