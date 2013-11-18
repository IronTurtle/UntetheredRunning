/* Bluetooth Mate Echo
  by: Jim Lindblom - jim at sparkfun.com
  date: 3/15/11
  license: CC-SA v3.0 - Use this code however you'd like, for any
  purpose. If you happen to find it useful, or make it better, let us know!
  
  This code allows you to send any of the RN-42 commands to the
  Bluetooth Mate via the Arduino Serial monitor. Characters sent
  over USB-Serial to the Arduino are relayed to the Mate, and
  vice-versa.
  
  Here are the connections necessary:
  Bluetooth Mate-----------------Arduino
      CTS-I    (not connected)
      VCC------------------------5V or 3.3V
      GND--------------------------GND
      TX-O-------------------------D2
      RX-I-------------------------D3
      RTS-O    (not connected)
  
  How to use:
  You can use the serial monitor to send any commands listed in
  the RN-42 Advanced User Manual
  (http://www.sparkfun.com/datasheets/Wireless/Bluetooth/rn-bluetooth-um.pdf)
  to the Bluetooth Mate.
  
  Open up the serial monitor to 9600bps, and make sure the 
  pull-down menu next to the baud rate selection is initially set
  to "No line ending". Now enter the configuration command $$$ in 
  the serial monitor and click Send. The Bluetooth mate should
  respond with "CMD".
  
  The RN-42 module expects a newline character after every command.
  So, once you're in command mode, change the "No line ending"
  drop down selection to "Newline". To test, send a simple command.
  For instance, try looking for other bluetooth devices by sending
  the I command. Type I and click Send. The Bluetooth Mate should
  respond with "Inquiry, COD", follwed by any bluetooth devices
  it may have found.
  
  To exit command mode, either connect to another device, or send
  ---.
  
  The newline and no line ending selections are very important! If
  you don't get any response, make sure you've set that menu correctly.
*/

// We'll use the newsoftserial library to communicate with the Mate
#include <SoftwareSerial.h>  

int led = 13;

void setup()
{
  pinMode(led, OUTPUT);
  
  Serial.begin(115200);  // Begin the serial monitor at 9600bps
  Serial.print("$$$");
  delay(100);
}

void loop()
{ 
  if(Serial.available())  // If stuff was typed in the serial monitor
  { 
    char val = Serial.read();
    
    if(val == 1)
    {
      digitalWrite(led,HIGH);
      Serial.println("LED ON");
    }
    else
    {
      digitalWrite(led, LOW);
      Serial.println("LED OFF");
    }
  }
  // and loop forever and ever!
}
