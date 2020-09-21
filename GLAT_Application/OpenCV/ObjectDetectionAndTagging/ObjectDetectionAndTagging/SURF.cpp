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
	Mat mDescriptors;
	vector<KeyPoint> mKeypoints;
	ImageFeatures(Mat image) {
		// 1: Detect Keypoints using SURF Detector
		Ptr<SURF> detector = SURF::create(minHessian);
		detector->detect(image, mKeypoints);
		// 2: Calculate descriptor (feature vectors)
		detector->compute(image, mKeypoints, mDescriptors);
	}
};

std::vector<Point2f> surfedImage(Mat object_image, Mat scene_image) {

	ImageFeatures object = ImageFeatures(object_image);
	ImageFeatures scene = ImageFeatures(scene_image);
	/*
	//-- Step 1: Detect the keypoints using SURF Detector
	int minHessian = 400;
	Ptr<SURF> detector = SURF::create(minHessian);
	//SurfFeatureDetector detector(minHessian);

	vector<KeyPoint> keypoints_object, keypoints_scene;
	detector->detect(object_image, keypoints_object);
	detector->detect(scene_image, keypoints_scene);

	//-- Step 2: Calculate descriptors (feature vectors)

	Mat descriptors_object, descriptors_scene;

	detector->compute(object_image, keypoints_object, descriptors_object);
	detector->compute(scene_image, keypoints_scene, descriptors_scene);
	*/
	//-- Step 3: Matching descriptor vectors using FLANN matcher
	FlannBasedMatcher matcher;
	std::vector< DMatch > matches;
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
	std::vector< DMatch > good_matches;

	for (int i = 0; i < object.mDescriptors.rows; i++)
	{
		if (matches[i].distance < 3 * min_dist)
		{
			good_matches.push_back(matches[i]);
		}
	}
	Mat img_matches, object_key_image;
	drawMatches(object_image, object.mKeypoints, scene_image, scene.mKeypoints,
		good_matches, img_matches, Scalar::all(-1), Scalar::all(-1),
		vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	//drawKeypoints(img_object_gray, keypoints_object, object_key_image, Scalar::all(-1), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
	resize(img_matches, object_key_image, Size(640, 480));
	imshow("Image Matches", object_key_image);
	waitKey(0);
	//-- Localize the object
	std::vector<Point2f> obj;
	std::vector<Point2f> sce;

	for (int i = 0; i < good_matches.size(); i++)
	{
		//-- Get the keypoints from the good matches
		obj.push_back(object.mKeypoints[good_matches[i].queryIdx].pt);
		sce.push_back(scene.mKeypoints[good_matches[i].trainIdx].pt);
	}

	Mat H = findHomography(obj, sce, CV_RANSAC);

	//-- Get the corners from the image_1 ( the object to be "detected" )
	std::vector<Point2f> obj_corners(4);
	obj_corners[0] = cvPoint(0, 0); obj_corners[1] = cvPoint(object_image.cols, 0);
	obj_corners[2] = cvPoint(object_image.cols, object_image.rows); obj_corners[3] = cvPoint(0, object_image.rows);
	std::vector<Point2f> scene_corners(4);

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
	String basePath = "D:/Visual Studio 2015/Projects/OpenCVGo/OpenCVGo/sample_images/";
	String basePath2 = "C:/Users/antar/Documents/GitHub/GLAT/OpenCV/ObjectDetectionAndTagging/ObjectDetectionAndTagging/Images/";
	Mat img_object = imread(basePath2+"watch1.jpg");
	Mat img_scene = imread(basePath2+"img1.jpg");
	Mat img_object_gray, img_scene_gray;
	Mat bgr[3];
	//resize(img_object, img_object, Size(640, 480));
	//resize(img_scene, img_scene, Size(640, 480));
	cvtColor(img_object, img_object_gray, CV_RGB2GRAY);
	cvtColor(img_scene, img_scene_gray, CV_RGB2GRAY);
	if (!img_object.data || !img_scene.data)
	{
		std::cout << " --(!) Error reading images " << std::endl; return -1;
	}
	std::vector<Point2f> scene_corners(4);
	scene_corners = surfedImage(img_object_gray, img_scene_gray);
	line(img_scene, scene_corners[0], scene_corners[1], Scalar(0,255,0), 4);
	line(img_scene, scene_corners[1], scene_corners[2], Scalar(0, 255, 0), 4);
	line(img_scene, scene_corners[2], scene_corners[3], Scalar(0, 255, 0), 4);
	line(img_scene, scene_corners[3], scene_corners[0], Scalar(0, 255, 0), 4);
	resize(img_scene, img_scene, Size(640, 480));
	imshow("Good Matches & Object detection", img_scene);
	/*
	Scalar colorpalatte[3] = { Scalar(255,0,0),Scalar(0,255,0), Scalar(0,0,255) };
	for (int i = 0; i < 3; i++) {
		split(img_object, bgr);
		img_object_gray = bgr[i];
		split(img_scene, bgr);
		img_scene_gray = bgr[i];
		scene_corners = surfedImage(img_object_gray, img_scene_gray);
		// Draw lines on scene image
		line(img_scene, scene_corners[0], scene_corners[1], colorpalatte[i], 4);
		line(img_scene, scene_corners[1], scene_corners[2], colorpalatte[i], 4);
		line(img_scene, scene_corners[2], scene_corners[3], colorpalatte[i], 4);
		line(img_scene, scene_corners[3], scene_corners[0], colorpalatte[i], 4);
		imshow("Good Matches & Object detection", img_scene);
	}
	*/
	waitKey(0);
	return 0;
}

/** @function readme */
void readme()
{
	std::cout << " Usage: ./SURF_descriptor <img1> <img2>" << std::endl;
}