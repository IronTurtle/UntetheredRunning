
//State constants
char const FRONT_BUZZ = 0;
char const LEFT_BUZZ = 1;
char const RIGHT_BUZZ = 2;
char const BACK_BUZZ = 3;
char const NO_BUZZ = 4;
char const EMERGENCY_BUZZ = 5;

int const BUZZ_DELAY = 200;

//Function prototypes
void singleMotor(int buzzerMotor);
void allBuzz();

//Motor pins
int rearMotor = 6;
int rearMotor_2 = 3;
int rightMotor = 13;
int leftMotor = 11;

//Variable that holds the commands from the BT serial port
char cmd = NO_BUZZ; 

void setup()
{
  //Set the motor pins to output
  pinMode(rearMotor, OUTPUT);
  pinMode(rearMotor_2, OUTPUT);
  pinMode(rightMotor, OUTPUT);
  pinMode(leftMotor, OUTPUT);
  
  // Begin the BT serial connection at 115200bps
  Serial.begin(115200);
}

void loop()
{
  if(Serial.available())
  {
    //Read the incoming byte
    cmd = Serial.read();
    //Send what was received back to Arduino for data checking
    Serial.print(cmd);
  }
  
  switch(cmd)
  {
    case FRONT_BUZZ:
      break; //Only the camera buzzes
    case LEFT_BUZZ:
      singleBuzz(leftMotor);
      break;
    case RIGHT_BUZZ:
      singleBuzz(rightMotor);
      break;
    case BACK_BUZZ:
      backBuzz();
      break;
    case NO_BUZZ:
      break; //No buzzers should go off
    case EMERGENCY_BUZZ:
      allBuzz();
      break;
  }
}

//Make a single buzzer go off
void singleBuzz(int buzzerNum)
{
  digitalWrite(buzzerNum, HIGH);
  delay(BUZZ_DELAY);
  digitalWrite(buzzerNum, LOW);
  delay(BUZZ_DELAY);
}

//Buzz the back motors in sync.
void backBuzz()
{
  digitalWrite(rearMotor, HIGH);
  digitalWrite(rearMotor_2, HIGH);
  delay(BUZZ_DELAY);
  digitalWrite(rearMotor, LOW);
  digitalWrite(rearMotor_2, LOW);
  delay(BUZZ_DELAY);
}

//Make all buzzers go off
void allBuzz()
{
  digitalWrite(leftMotor, HIGH);
  digitalWrite(rightMotor, HIGH);
  digitalWrite(rearMotor, HIGH);
  digitalWrite(rearMotor_2, HIGH);
  delay(BUZZ_DELAY);
  digitalWrite(leftMotor, LOW);
  digitalWrite(rightMotor, LOW);
  digitalWrite(rearMotor, LOW);
  digitalWrite(rearMotor_2, LOW);
  delay(BUZZ_DELAY);
}
