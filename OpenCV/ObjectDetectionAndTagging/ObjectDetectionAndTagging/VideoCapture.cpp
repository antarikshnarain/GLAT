#include<opencv2/highgui/highgui.hpp>
#include<opencv2/core/core.hpp>

using namespace cv;

int abc() {
	VideoCapture cap(0);
	Mat frame;
	while (cap.isOpened()) {
		if (!cap.read(frame))
			break;
		imshow("VIDEO", frame);
		if (waitKey(10) == 27) {
			break;
		}
	}
	return 0;
}