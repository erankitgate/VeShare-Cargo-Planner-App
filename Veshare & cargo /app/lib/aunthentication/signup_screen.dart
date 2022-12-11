import 'package:app/aunthentication/car_info_screen.dart';
import 'package:app/aunthentication/login_screen.dart';

import 'package:app/widgets/progress_dialog.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../global/global.dart';

class SignUpScreen extends StatefulWidget {
  const SignUpScreen({Key? key}) : super(key: key);

  @override
  State<SignUpScreen> createState() => _SignUpScreenState();
}

class _SignUpScreenState extends State<SignUpScreen> {


  TextEditingController nameTextEditingController=TextEditingController();
  TextEditingController emailTextEditingController=TextEditingController();
  TextEditingController phoneTextEditingController=TextEditingController();
  TextEditingController passwordTextEditingController=TextEditingController();

   validateForm()
   {
     if(nameTextEditingController.text.length<3)
       {
         Fluttertoast.showToast(msg:"Name must be atleast 3 character");
       }
     else if(!emailTextEditingController.text.contains("@"))
     {
       Fluttertoast.showToast(msg:"Email is not valid");
     }
     else if(phoneTextEditingController.text.isEmpty)
       {
         Fluttertoast.showToast(msg:"Phone Number is required");
       }
     else if(passwordTextEditingController.text.length<8)

       {
         Fluttertoast.showToast(msg:"Password must be atleast 8 character ");

       }
     else
       {
         saveDriverInfoNow();
       }
   }
  saveDriverInfoNow() async
  {
    showDialog(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext c)
        {
          return ProgressDialog(message: "Processing, Please wait...",);
        }
    );

    final User? firebaseUser = (
        await fAuth.createUserWithEmailAndPassword(
          email: emailTextEditingController.text.trim(),
          password: passwordTextEditingController.text.trim(),
        ).catchError((msg){
          Navigator.pop(context);
          Fluttertoast.showToast(msg: "Error: " + msg.toString());
        })
    ).user;

    if(firebaseUser != null) {
      Map driverMap =
      {
        "id": firebaseUser.uid,
        "name": nameTextEditingController.text.trim(),
        "email": emailTextEditingController.text.trim(),
        "phone": phoneTextEditingController.text.trim(),
      };

      DatabaseReference driversRef = FirebaseDatabase.instance.ref().child("drivers");
      driversRef.child(firebaseUser.uid).set(driverMap);

      currentFirebaseUser = firebaseUser;
      Fluttertoast.showToast(msg: "Account has been Created.");
      Navigator.push(context, MaterialPageRoute(builder: (c)=> CarInfoScreen()));

    }
    else{
      Fluttertoast.showToast(msg: "Account has not been Created.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.blueGrey.shade800,
      body: SingleChildScrollView(
         child: Padding(
           padding: const EdgeInsets.all(16.0),
           child: Column(
             children: [

               const SizedBox(height: 10,),

               Padding(
                 padding: const EdgeInsets.all(8.0),
                 child: Image.asset("images/logo1.png"),
               ),
               const SizedBox(height: 10,),
               const Text(
                 "Register as a Host",
                 style:  TextStyle(
                   fontSize: 26,
                   color: Colors.grey,
                   fontWeight: FontWeight.bold,

                 ),
               ),

              TextField(
                controller: nameTextEditingController,
                style: const TextStyle(
                  color: Colors.grey
                ),
                decoration: const InputDecoration(
                  labelText: "Name",
                  hintText: "Name",

                  enabledBorder:  UnderlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey),
                ),

                  focusedBorder:  UnderlineInputBorder(
                      borderSide: BorderSide(color: Colors.grey),
                  ),
                  hintStyle: TextStyle(
                    color: Colors.grey,
                    fontSize: 10
                  ),
                    labelStyle: TextStyle(
                    color: Colors.grey,
                    fontSize: 14
                )

                ) ,




              ),

               TextField(
                 controller: emailTextEditingController,
                 keyboardType: TextInputType.emailAddress,
                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Email",
                     hintText: "Email",

                     enabledBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),

                     focusedBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),
                     hintStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 10
                     ),
                     labelStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 14
                     )

                 ) ,




               ),

               TextField(
                 controller: phoneTextEditingController,
                 keyboardType: TextInputType.number,
                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Phone",
                     hintText: "Phone",

                     enabledBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),

                     focusedBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),
                     hintStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 10
                     ),
                     labelStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 14
                     )

                 ) ,




               ),

               TextField(
                 controller: passwordTextEditingController,


                 obscureText: true,
                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Password",
                     hintText: "Password",

                     enabledBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),

                     focusedBorder:  UnderlineInputBorder(
                       borderSide: BorderSide(color: Colors.grey),
                     ),
                     hintStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 10
                     ),
                     labelStyle: TextStyle(
                         color: Colors.grey,
                         fontSize: 14
                     )

                 ) ,




               ),
               const SizedBox(height: 20,),

               ElevatedButton(

                 onPressed: ()
                 {
                   validateForm();

                 },
                 style: ElevatedButton.styleFrom(
                   primary: Colors.lightGreenAccent,
                 ),
                 child: const Text(
                   "Create Account",
                       style: TextStyle(
                         color: Colors.black54,
                         fontSize: 18,


                 )

                 ),

               ),
               TextButton(
                 child: const Text(
                     "Already have account? Login  ",
                         style: TextStyle(color: Colors.grey),

                 ),
                 onPressed: ()
                 {
                   Navigator.push(context, MaterialPageRoute(builder: (c)=> LoginScreen()));
                 },
               ),


             ],
           ),
         ),
      ),
    );
  }
}
