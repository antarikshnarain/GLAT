#include <stdio.h>
#include <iostream>
#include <ctime>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/videoio/videoio.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/xfeatures2d/nonfree.hpp>
#include <opencv2/xfeatures2d.hpp>

using namespace std;
using namespace cv;
using namespace cv::xfeatures2d;
//using namespace xfeatures2d;
void readme();

class ImageFeatures {
	const int minHessian = 400;
public:
	Mat image;
	Mat mDescriptors;
	vector<KeyPoint> mKeypoints;
	ImageFeatures() {
	}
	ImageFeatures(Mat image) {
		this->image = image;
		// 1: Detect Keypoints using SURF Detector
		Ptr<SURF> detector = SURF::create(minHessian);
		detector->detect(image, mKeypoints);
		// 2: Calculate descriptor (feature vectors)
		detector->compute(image, mKeypoints, mDescriptors);
	}
	ImageFeatures(Mat image, int code) {
		cvtColor(image, this->image, code);
		Ptr<SURF> detector = SURF::create(minHessian);
		detector->detect(this->image, mKeypoints);
		// 2: Calculate descriptor (feature vectors)
		detector->compute(this->image, mKeypoints, mDescriptors);
	}
};

vector<Point2f> surfedImage(ImageFeatures object, ImageFeatures scene) {

	//-- Step 3: Matching descriptor vectors using FLANN matcher
	FlannBasedMatcher matcher;
	vector< DMatch > matches;
	vector<Point2f> scene_corners(4);

	try {
		matcher.match(object.mDescriptors, scene.mDescriptors, matches);
	}
	catch (Exception e) {
		return scene_corners;
	}
	double max_dist = 0; double min_dist = 100;

	//-- Quick calculation of max and min distances between keypoints
	for (int i = 0; i < object.mDescriptors.rows; i++)
	{
		double dist = matches[i].distance;
		if (dist < min_dist) min_dist = dist;
		if (dist > max_dist) max_dist = dist;
	}

	printf("-- Max dist : %f \n", max_dist);
	printf("-- Min dist : %f \n", min_dist);

	//-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
	vector< DMatch > good_matches;

	for (int i = 0; i < object.mDescriptors.rows; i++)
	{
		if (matches[i].distance < 3 * min_dist)
		{
			good_matches.push_back(matches[i]);
		}
	}
	Mat img_matches, object_key_image;
	/*
	Function to draw match lines between the object and the scene image 
	Return: Image with match lines
	*/
	//drawMatches(object.image, object.mKeypoints, scene.image, scene.mKeypoints,good_matches, img_matches, Scalar::all(-1), Scalar::all(-1),vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	//imshow("Image Matches", img_matches);
	/*
	Function to to mark feature points on the Image
	Return: Image with feature points.
	*/
	//drawKeypoints(img_object_gray, keypoints_object, object_key_image, Scalar::all(-1), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	//imshow("Image Matches", object_key_image);
	//waitKey(0);
	//-- Localize the object
	vector<Point2f> obj;
	vector<Point2f> sce;
	cout << "\n======================" << good_matches.size();
	for (int i = 0; i < good_matches.size(); i++)
	{
		//-- Get the keypoints from the good matches
		obj.push_back(object.mKeypoints[good_matches[i].queryIdx].pt);
		sce.push_back(scene.mKeypoints[good_matches[i].trainIdx].pt);
	}

	Mat H = findHomography(obj, sce, CV_RANSAC);
	// To Avoid Error:Assertion failed (scn + 1 == m.cols) in cv::perspectiveTransform -> dim(H) = 2 (3x3)
	if (H.dims < 2)
		return scene_corners;
	//-- Get the corners from the image_1 ( the object to be "detected" )
	vector<Point2f> obj_corners(4);
	obj_corners[0] = cvPoint(0, 0); obj_corners[1] = cvPoint(object.image.cols, 0);
	obj_corners[2] = cvPoint(object.image.cols, object.image.rows); obj_corners[3] = cvPoint(0, object.image.rows);

	perspectiveTransform(obj_corners, scene_corners, H);
	cout << scene_corners;

	return scene_corners;
	//-- Draw lines between the corners (the mapped object in the scene - image_2 )
	// Draw lines on the stitched image
	//line(img_matches, scene_corners[0] + Point2f(img_object_gray.cols, 0), scene_corners[1] + Point2f(img_object_gray.cols, 0), Scalar(0, 255, 0), 4);
	//line(img_matches, scene_corners[1] + Point2f(img_object_gray.cols, 0), scene_corners[2] + Point2f(img_object_gray.cols, 0), Scalar(0, 255, 0), 4);
	//line(img_matches, scene_corners[2] + Point2f(img_object_gray.cols, 0), scene_corners[3] + Point2f(img_object_gray.cols, 0), Scalar(0, 255, 0), 4);
	//line(img_matches, scene_corners[3] + Point2f(img_object_gray.cols, 0), scene_corners[0] + Point2f(img_object_gray.cols, 0), Scalar(0, 255, 0), 4);
}
Mat borderedObject(Mat img_matches, vector<Point2f> scene_corners) {
	if (scene_corners.empty())
		return img_matches;
	// Checking boundary
	for (int i = 0; i < 4; i++) {
		Point2f p = scene_corners[i];
		// if any point out of frame
		if (p.x > img_matches.cols || p.y > img_matches.rows)
			return img_matches;
		// To avoid points or lines in matching
		/*
		for (int j = i+1; j < 4; j++) {
			Point2f q = scene_corners[j];
			if (abs(p.x - q.x) < 10 || abs(p.x - q.x) < 10)
				return img_matches;
		}*/
	}
	line(img_matches, scene_corners[0], scene_corners[1], Scalar(0, 255, 0), 4);
	line(img_matches, scene_corners[1], scene_corners[2], Scalar(0, 255, 0), 4);
	line(img_matches, scene_corners[2], scene_corners[3], Scalar(0, 255, 0), 4);
	line(img_matches, scene_corners[3], scene_corners[0], Scalar(0, 255, 0), 4);
	return img_matches;
}
Rect getRect(const Mat im, const string win_name);
static void onMouse(int event, int x, int y, int flags, void *param);
int main(int argc, char** argv)
{
	int i, j;
	String basePath2 = "C:/Users/antar/Documents/GitHub/GLAT/OpenCV/ObjectDetectionAndTagging/ObjectDetectionAndTagging/Images/";
	Mat img_object = imread(basePath2 + "jiofi.png");
	resize(img_object, img_object, Size(480, 640));
	Mat img_scene, img_scene_gray, object_key_image, mDescriptors;
	ImageFeatures object, scene;
	VideoCapture cap(0);
	bool show_preview = true;
	if (!cap.isOpened()) {
		cerr << "Unable to open video capture." << endl;
		return -1;
	}
	//Show preview until key is pressed
	while (show_preview)
	{
		Mat preview;
		cap >> preview;
		imshow("Preview", preview);

		char k = waitKey(10);
		if (k != -1) {
		show_preview = false;
		}
	}
	//Get initial image
	Mat im0;
	cap >> im0;
	//If no bounding was specified, get it from user
	Rect rect = getRect(im0, "Preview");
	img_object = im0(rect);

	object = ImageFeatures(img_object, CV_RGB2GRAY);
	cout << object.mKeypoints.size() << endl;

	int start_s = clock();
	for (i = 0; i < 100; i++) {
		cout << "\nProcessing Frame " << i;
		if (cap.isOpened() && cap.read(img_scene)) {
			scene = ImageFeatures(img_scene, CV_RGB2GRAY);
			imshow("WOW", borderedObject(img_scene, surfedImage(object, scene)));
			waitKey(2);
		}
	}
	int stop_s = clock();
	cout << "time: " << (stop_s - start_s) / double(CLOCKS_PER_SEC) * 1000 << endl;
	waitKey(0);
	return 0;
}


//For bbox selection
static string win_name_;
static Mat im_select;
static bool tl_set;
static bool br_set;
static Point tl;
static Point br;

static void onMouse(int event, int x, int y, int flags, void *param)
{
	Mat im_draw;
	im_select.copyTo(im_draw);

	if (event == CV_EVENT_LBUTTONUP && !tl_set)
	{
		tl = Point(x, y);
		tl_set = true;
	}

	else if (event == CV_EVENT_LBUTTONUP && tl_set)
	{
		br = Point(x, y);
		br_set = true;
	}

	else
	{
		rectangle(im_draw, tl, Point(x, y), Scalar(255, 0, 0));
	}

	imshow(win_name_, im_draw);
}

Rect getRect(const Mat im)
{

	win_name_ = "GetRect";
	im_select = im;
	tl_set = false;
	br_set = false;

	setMouseCallback("GetRect", onMouse);

	//Dummy call to get drawing right
	onMouse(0, 0, 0, 0, 0);

	while (!br_set)
	{
		cvWaitKey(10);
	}

	setMouseCallback("GetRect", NULL);

	im_select.release(); //im_select is in global scope, so we call release manually

	return Rect(tl, br);
}