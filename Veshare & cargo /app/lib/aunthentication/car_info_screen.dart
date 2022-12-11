import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../global/global.dart';
import '../splashScreen/splash_screen.dart';

class CarInfoScreen extends StatefulWidget {


  @override
  State<CarInfoScreen> createState() => _CarInfoScreenState();
}

class _CarInfoScreenState extends State<CarInfoScreen> {


  TextEditingController carModelTextEditingController=TextEditingController();
  TextEditingController carNumberTextEditingController=TextEditingController();
 // TextEditingController carColorTextEditingController=TextEditingController();
  TextEditingController sourceTextEditingController=TextEditingController();
  TextEditingController destinationTextEditingController=TextEditingController();

  TextEditingController timeTextEditingController=TextEditingController();
  TextEditingController chargesTextEditingController=TextEditingController();
  List<String> carTypesList=["Maruti-Swift","i10","bike"];
  String? selectedCarType;

  saveCarInfo()
  {
    Map driverCarInfoMap =
    {
      // "car_color": carColorTextEditingController.text.trim(),
      "car_number": carNumberTextEditingController.text.trim(),
      "car_model": carModelTextEditingController.text.trim(),
      "source":sourceTextEditingController.text.trim(),
      "destination":destinationTextEditingController.text.trim(),
      "time":timeTextEditingController.text.trim(),
      "charges":chargesTextEditingController.text.trim(),
      "type": selectedCarType,
    };

    DatabaseReference driversRef = FirebaseDatabase.instance.ref().child("drivers");
    driversRef.child(currentFirebaseUser!.uid).child("car_details").set(driverCarInfoMap);

    Fluttertoast.showToast(msg: "Car Details has been saved, Congratulations.");
    Navigator.push(context, MaterialPageRoute(builder: (c)=> const MySplashScreen()));
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.blueGrey.shade800,
      body: SingleChildScrollView(
         child: Padding(
           padding: const EdgeInsets.all(20.0),
           child: Column(
             children: [
               const SizedBox(height: 24  ,),
               Padding(
                 padding: const EdgeInsets.all(20.0),
                 child: Image.asset("images/logo1.png"),
               ),
               const SizedBox(height: 10,),
               const Text(
                 "Host Vechile Details",
                 style:  TextStyle(
                   fontSize: 26,
                   color: Colors.grey,
                   fontWeight: FontWeight.bold,

                 ),
               ),
               TextField(
                 controller: carModelTextEditingController,
                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Vechile Model",
                     hintText: "Vechile Model",

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
                 controller: carNumberTextEditingController,

                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Vechile Number",
                     hintText: "Vechile Number",

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
                 controller:sourceTextEditingController,

                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Source",
                     hintText: "Source",

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
                 controller:destinationTextEditingController,

                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Destination",
                     hintText: "Destination",

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
                 controller:timeTextEditingController,

                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Time",
                     hintText: "Time",

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
                 controller:chargesTextEditingController,

                 style: const TextStyle(
                     color: Colors.grey
                 ),
                 decoration: const InputDecoration(
                     labelText: "Charges",
                     hintText: "Charges",

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
               const SizedBox(height: 20  ,),
               DropdownButton(
                 iconSize: 26,

                 dropdownColor: Colors.black,
                 hint: const Text(
                   "Please Choose Vechile Type",
                   style: TextStyle(
                     fontSize: 14.0,
                     color: Colors.grey,

                   )
                 ),
                 value: selectedCarType,
                 onChanged: (newValue)
                   {
                     setState((){
                      selectedCarType=newValue.toString() ;
                     });
                   },

                   items:  carTypesList.map((car)
                  {
                    return  DropdownMenuItem(
                    child: Text(
                    car,
                    style: const TextStyle(color:Colors.grey),
                    ),
                   value:car,
                    );
                  }).toList(),

               ),
               const SizedBox(height: 20,),

               ElevatedButton(
                 onPressed: ()
                 {

                   if( carNumberTextEditingController.text.isNotEmpty
                       && carModelTextEditingController.text.isNotEmpty && sourceTextEditingController.text.isNotEmpty &&destinationTextEditingController.text.isNotEmpty  && timeTextEditingController.text.isNotEmpty &&chargesTextEditingController.text.isNotEmpty  && selectedCarType != null)
                   {
                     saveCarInfo();
                   }

                 },
                 style: ElevatedButton.styleFrom(
                   primary: Colors.lightGreenAccent,
                 ),
                 child: const Text(
                     "Save Now",
                     style: TextStyle(
                       color: Colors.black54,
                       fontSize: 18,


                     )

                 ),

               ),

             ],
           ),
         ),
      ),
    );
  }
}
