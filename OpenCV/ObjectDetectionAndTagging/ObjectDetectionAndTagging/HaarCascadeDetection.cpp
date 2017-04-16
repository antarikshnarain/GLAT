#include <opencv2/objdetect.hpp>
#include <opencv2/videoio/videoio.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <stdio.h>
using namespace std;
using namespace cv;
void detectAndDisplay(Mat frame);
String face_cascade_name = "watch.xml";//"haarcascade_frontalface_default.xml";
String eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
String watch_cascade_name = "watch.xml";
CascadeClassifier face_cascade;
CascadeClassifier eyes_cascade;
String window_name = "Capture - Face detection";
int main(void)
{
	VideoCapture capture;
	Mat frame;
	String basePath = "D:/Visual Studio 2015/Projects/OpenCVGo/OpenCVGo/data/haarcascades/";
	//-- 1. Load the cascades

	if (!eyes_cascade.load(basePath + eyes_cascade_name)) { printf("--(!)Error loading eyes cascade\n"); return -1; };
	if (!face_cascade.load(basePath + face_cascade_name)) { printf("--(!)Error loading face cascade\n"); return -2; };
	//-- 2. Read the video stream
	String scene_img_path = "C:/Users/antar/Documents/GitHub/GLAT/OpenCV/ObjectDetectionAndTagging/ObjectDetectionAndTagging/Images/watch1.jpg";
	Mat scene_image = imread(scene_img_path);
	imshow("Image",scene_image);
	waitKey(60);
	detectAndDisplay(scene_image);
	waitKey(60);
	getchar();
	/*
	capture.open(0);
	if (!capture.isOpened()) { printf("--(!)Error opening video capture\n"); return -1; }
	capture.retrieve(frame);
	while (capture.read(frame))
	{
		if (frame.empty())
		{
			printf(" --(!) No captured frame -- Break!");
			break;
		}
		//-- 3. Apply the classifier to the frame
		detectAndDisplay(frame);
		char c = (char)waitKey(1);
		if (c == 27) { break; } // escape
	}*/
	return 0;
}
void detectAndDisplay(Mat frame)
{
	vector<Rect> faces;
	Mat frame_gray;
	cvtColor(frame, frame_gray, COLOR_BGR2GRAY);

	equalizeHist(frame_gray, frame_gray);
	//-- Detect faces
	face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0 | CASCADE_SCALE_IMAGE, Size(10, 10));
	cout << faces.size();
	for (size_t i = 0; i < faces.size(); i++)
	{
		Point center(faces[i].x + faces[i].width / 2, faces[i].y + faces[i].height / 2);
		cout << center;
		ellipse(frame, center, Size(faces[i].width / 2, faces[i].height / 2), 0, 0, 360, Scalar(255, 0, 255), 4, 8, 0);
		Mat faceROI = frame_gray(faces[i]);
		std::vector<Rect> eyes;
		//-- In each face, detect eyes
		eyes_cascade.detectMultiScale(faceROI, eyes, 1.1, 2, 0 | CASCADE_SCALE_IMAGE, Size(30, 30));
		for (size_t j = 0; j < eyes.size(); j++)
		{
			Point eye_center(faces[i].x + eyes[j].x + eyes[j].width / 2, faces[i].y + eyes[j].y + eyes[j].height / 2);
			int radius = cvRound((eyes[j].width + eyes[j].height)*0.25);
			circle(frame, eye_center, radius, Scalar(255, 0, 0), 4, 8, 0);
		}
	}
	//-- Show what you got
	imshow(window_name, frame);
}