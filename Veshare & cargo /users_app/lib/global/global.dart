import 'package:firebase_auth/firebase_auth.dart';
import 'package:users_app/models/direction_details_info.dart';
import 'package:users_app/models/user_model.dart';



final FirebaseAuth fAuth = FirebaseAuth.instance;
User? currentFirebaseUser;
UserModel? userModelCurrentInfo;
List dList = []; //online-active drivers Information List
DirectionDetailsInfo? tripDirectionDetailsInfo;
String? chosenDriverId="";
String cloudMessagingServerToken = "key=AAAAsMydiKw:APA91bF-Cbe9RBz-Xy_OyBFYIwq5IalzikdaxR4kEy1NNgx2GlunsCAXBIfihze6Z_kH04myk__pa6SVeoHJUqjrsb2eK6wRAo-dFDmPhVOwhiw039qEVZjBX-25BjtVRQrQbhoR9eq7";
String userDropOffAddress = "";
String driverCarDetails="";
String driverName="";
String driverPhone="";
double countRatingStars=0.0;
String titleStarsRating="";