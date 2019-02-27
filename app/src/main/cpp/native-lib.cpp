#include <jni.h>
#include <string>
#include <iostream>
#include <Poco/URI.h>
#include <Poco/Net/HTTPClientSession.h>
#include <Poco/Net/HTTPResponse.h>
#include <Poco/Net/HTTPRequest.h>
#include <sstream>


extern "C" JNIEXPORT jstring JNICALL
Java_cc_mil_cnt_cream_1sauce_1smoked_1chicken_1spaghetti_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    Poco::URI uri("http://122.116.78.229/a.xml");
    std::string path(uri.getPathAndQuery());
    if (path.empty()) path = "/";
    Poco::Net::HTTPClientSession session(uri.getHost(), uri.getPort());
    Poco::Net::HTTPRequest request(Poco::Net::HTTPRequest::HTTP_GET, path);
    Poco::Net::HTTPResponse response;

    session.sendRequest(request);
    std::istream& rs = session.receiveResponse(response);
    if(response.getStatus() != Poco::Net::HTTPResponse::HTTP_OK){
        return env->NewStringUTF("http request error\n");
    }
    std::ostringstream os;
    std::string resp_s;
    os << rs.rdbuf();
    resp_s = os.str();


    std::string hello = "Hello from C++";
    return env->NewStringUTF(resp_s.c_str());
}