import 'dart:async';

import 'package:flutter/material.dart';

import '../assistants/assistant_methods.dart';
import '../authentication/login_screen.dart';
import '../global/global.dart';
import '../mainScreens/main_screen.dart';




class MySplashScreen extends StatefulWidget {
  const MySplashScreen({Key? key}) : super(key: key);

  @override
  State<MySplashScreen> createState() => _MySplashScreenState();
}

class _MySplashScreenState extends State<MySplashScreen> {

 startTimer()
 {
   fAuth.currentUser != null ? AssistantMethods.readCurrentOnlineUserInfo() : null;
   Timer(const Duration(seconds: 3),() async
   {
     //send user to home screen
     if(await fAuth.currentUser != null)
     {
       currentFirebaseUser = fAuth.currentUser;
       Navigator.push(context, MaterialPageRoute(builder: (c)=> MainScreen()));
     }
     else
     {
       Navigator.push(context, MaterialPageRoute(builder: (c)=> LoginScreen()));
     }

   });
  }
  @override
  void initState() {

    super.initState();
    startTimer();
  }
  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.blueGrey.shade800,
      child: Center(
        child:Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children:[
             Image.asset("images/logo1.png"),

            const SizedBox(height:1 ,),


          ],
        )
      ),

    );
  }
}
