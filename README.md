# GLAT

Capstone Project 

## Android Application
Application that gives non-IoT objects, an identity and an interface to connect to the Internet.

1. Install Android Studio
2. Install Visual Studio
3. Install OpenCV 3.2.0 with extra modules
4. Import Project in Android Studio
5. Build APK
6. Import Object Detection in Visual Studio
7. Build Binary using Linux or Visual Studio.
```bash
g++ ImageSURFDetection.cpp -o app `pkg-config --cflags --libs opencv`
```
8. Deploy the file on server.

## Server Instruction

1. Install PostgresSQL
  - Make user
2. Install Redis Server
3. Install NodeJS
4. Install Solr
  - Create a solr core using command: bin/solr create -c glat
5. Run this in terminal: git clone https://github.com/glear14195/glat (in this case code present on CD)
6. Run: npm install 
7. copy lib/config.sample to lib/config.js and change values according to postgres details.
8. Run all SQL statements in release/prefetch.sql by logging into postgres using the command: psql -h localhost -U 'username' -d 'database_name' 
9. Run: node one_time/solr_index.js
10. Run: node try1.js &
11. Run: node kuejobs.js &

*Note: Step by step can be found at [GitHub](https://github.com/glear14195/glat)*

## Demo Video
[YouTube](https://youtu.be/4458Zgn-Ntw)
