#include <stdio.h>
#include <iostream>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/xfeatures2d/nonfree.hpp>
#include <opencv2/xfeatures2d.hpp>

using namespace std;
using namespace cv;
using namespace cv::xfeatures2d;
//using namespace xfeatures2d;
void readme();

class ImageFeatures {
	int minHessian = 400;
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
};

vector<Point2f> surfedImage(ImageFeatures object, ImageFeatures scene) {

	//-- Step 3: Matching descriptor vectors using FLANN matcher
	FlannBasedMatcher matcher;
	vector< DMatch > matches;
	matcher.match(object.mDescriptors, scene.mDescriptors, matches);

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
	//drawMatches(object.image, object.mKeypoints, scene.image, scene.mKeypoints,
	//	good_matches, img_matches, Scalar::all(-1), Scalar::all(-1),
	//	vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	//drawKeypoints(img_object_gray, keypoints_object, object_key_image, Scalar::all(-1), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	//imshow("Image Matches", object_key_image);
	//waitKey(0);
	//-- Localize the object
	vector<Point2f> obj;
	vector<Point2f> sce;

	for (int i = 0; i < good_matches.size(); i++)
	{
		//-- Get the keypoints from the good matches
		obj.push_back(object.mKeypoints[good_matches[i].queryIdx].pt);
		sce.push_back(scene.mKeypoints[good_matches[i].trainIdx].pt);
	}

	Mat H = findHomography(obj, sce, CV_RANSAC);

	//-- Get the corners from the image_1 ( the object to be "detected" )
	vector<Point2f> obj_corners(4);
	obj_corners[0] = cvPoint(0, 0); obj_corners[1] = cvPoint(object.image.cols, 0);
	obj_corners[2] = cvPoint(object.image.cols, object.image.rows); obj_corners[3] = cvPoint(0, object.image.rows);
	vector<Point2f> scene_corners(4);

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
int main(int argc, char** argv)
{
	int i, j;
	String basePath2 = "C:/Users/antar/Documents/GitHub/GLAT/OpenCV/ObjectDetectionAndTagging/ObjectDetectionAndTagging/Images/";
	Mat img_object = imread(basePath2 + "chair01.png");
	resize(img_object, img_object, Size(480, 640));
	Mat img_scene,img_scene_gray, object_key_image, mDescriptors;
	Ptr<SURF> detector = SURF::create(400);
	vector<KeyPoint> mKeypoints;
	VideoCapture cap(0);
	for (i = 0; i < 100; i++) {
		cout << "\n\nProcessing Frame " << i << endl;
		if (cap.isOpened() && cap.read(img_scene)) {
			detector->detect(img_scene, mKeypoints);
			detector->compute(img_scene, mKeypoints, mDescriptors);
			drawKeypoints(img_scene, mKeypoints, img_scene, Scalar::all(-1), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
			imshow("Good Matches & Object detection", img_scene);
			cout << cap.get(CV_CAP_PROP_FPS)<<endl;
			waitKey(1);
		}
	}

	waitKey(0);
	return 0;
}

/** @function readme */
void readme()
{
	cout << " Usage: ./SURF_descriptor <img1> <img2>" << endl;
}