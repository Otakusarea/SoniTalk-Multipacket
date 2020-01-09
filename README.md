# SoniTalk
SoniTalk is a novel open and transparent protocol for ultrasonic communication between devices such as smartphones, TVs, and IoT devices. Thereby SoniTalk gives the user full control over her privacy by a fine grained permission system.

Ultrasonic communication (UC) is increasingly used for data exchange between mobile phones and other devices, as well as for location-based services. UC is attractive because it is inaudible and very low-threshold in terms of the hardware required (only microphone and speaker required). Today, there exist several proprietary solutions for UC on the market, which are developed by companies in a closed source form, which raises questions regarding the protection of users' privacy, since it is not clear which data is actually sent, when and to whom.

The project was pursued by Matthias Zeppelzauer, Alexis Ringot and Florian Taurer at the Media Computing Group of the University of Applied Sciences in St. Pölten. In the future, SoniTalk could benefit various industries as well as end consumers with new communication possibilities.

The project website of the SoniTalk project with all published results and resources can be found here: [sonitalk.fhstp.ac.at](https://sonitalk.fhstp.ac.at/).

During the master thesis, called UNITA - Ultrasonic Network for IoT Applications, of Florian Taurer, SoniTalk got extended by a multipacket feature. The possibility of splitting longer messages into shorter message packets got implemented.

## Getting started
### Prerequisites
#### Minimum SDK
The minimum SDK required is 23 because we should give the users the right NOT to communicate over sound. This can only be achieved if they can deny the permission in the app. This restriction could be adapted for application where no user is involved (e.g. communication between IoT devices).

#### Permissions
The SoniTalk Demo app needs access to the microphone for receiving messages and the custom data-over-sound permission to send and receive messages.

### How to include the SDK
You can download our SDK as an AAR library, or add the source code as a module in your project.

### How to use the SDK
You will find below a walkthrough of the steps needed to use SoniTalk to exchange data over sound. Please see the SoniTalk Demo app for more details on how to use the SDK.

#### SoniTalkContext
An instance of the class SoniTalkContext is needed to create SoniTalkEncoder, SoniTalkSender and SoniTalkDecoder objects. On creation you need to pass to the context constructor a ResultReceiver which will receive callbacks from SoniTalk. For more details, please see the Javadoc.
In onCreate:

```java
  soniTalkPermissionsResultReceiver = new SoniTalkPermissionsResultReceiver(new Handler());
  soniTalkPermissionsResultReceiver.setReceiver(this); // To receive callbacks from the SDK
  soniTalkContext = SoniTalkContext.getInstance(this, soniTalkPermissionsResultReceiver);
```

Then in onStart:

```java
  soniTalkPermissionsResultReceiver.setReceiver(this);
```

And in onStop (we would also recommend to stop all Decoders/Senders in order to avoid keeping infinite notifications):

```java
  soniTalkPermissionsResultReceiver.setReceiver(null);
```

#### SoniTalkConfig
A SoniTalkConfig object needs to be created in order to receive or send messages. The configuration should be the same for the Encoder and Decoder apps. The default configuration is in the near ultrasound range and should not be hearable by adults.

```java
SoniTalkConfig config = ConfigFactory.getDefaultConfig(this.getApplicationContext());
```

For more configuration options, please see the Javadoc and specification document.

#### Receiving messages
Get a SoniTalkDecoder object, add a listener for the results, and start listening by calling receiveBackground(), you can also specify how long it should record before stopping (i.e. a sort of timeout).

```java
soniTalkDecoder = soniTalkContext.getDecoder(samplingRate, config);
soniTalkDecoder.addMessageListener(this); // "this" will be notified of messages received (calls onMessageReceived callback)
soniTalkDecoder.receiveBackground(RECEIVING_REQUEST_CODE);
```

Then, when a message is received, onMessageReceived is called (you need to override it):

```java
@Override
public void onMessageReceived(final SoniTalkMessage receivedMessage) {
  // Process the message received
}
```

If your message was UTF8 text you can use:

```java
final String textReceived = DecoderUtils.byteToUTF8(receivedMessage.getMessage());
```

When you are done with receiving (e.g. in onMessageReceived if(receivedMessage.isCrcCorrect())), please stop the Decoder:

```java
soniTalkDecoder.stopReceiving();
```

You might also want to implement onDecoderError (e.g. in case the microphone is not available), to stop the decoder and notify the user if a problem occur:

```java
@Override
public void onDecoderError(final String errorMessage)
```

#### Sending messages
Sending a message is done in two steps. The first one is to generate the audio signal and the second one is to actually transmit via the loudspeaker.

With the SoniTalkContext an object of SoniTalkEncoder can be created by using the method getEncoder(SoniTalkConfig). Then, a message can be generated by calling the method generateMessage(byte[]) of the encoder object. The byte array passed as a parameter is the data to be exchanged. You can easily transform a String to byte if you know its Charset. The encoder then returns a SoniTalkMessage, which can be sent via a SoniTalkSender. To instantiate a SoniTalkSender you can call the method getSender() of SoniTalkContext. After that, the function send can be executed with the message object and a request code (this request code will then be passed in the callback for you to know which message was sent, or if the user denied the permission to send it).

##### Message encoding
If you are sending UTF8 text, you can transform it with:

```java
final byte[] bytes = textToSend.getBytes(StandardCharsets.UTF_8);
```

And then generate the message from the byte array:

```java
soniTalkEncoder = soniTalkContext.getEncoder(config);
soniTalkMessage = soniTalkEncoder.generateMessage(bytes);
```

##### Message sending
Before sending you can use EncoderUtils.isAllowedByteArraySize(bytes, config) to check if your message will fit in one SoniTalkMessage.

```java
soniTalkSender = soniTalkContext.getSender();
soniTalkSender.send(currentMessage, SENDING_REQUEST_CODE);
```

#### Callbacks:
The communication between your app and the SoniTalk SDK is based on several callbacks.  Especially, the whole permission system is using the soniTalkPermissionsResultReceiver that you pass to the SoniTalkContext.

At least the "negative callbacks" should be used to show some rationale to the user when they refuse to give permissions. Namely: ON_PERMISSION_LEVEL_DECLINED, ON_REQUEST_DENIED, ON_REQUEST_L0_DENIED (or ON_SHOULD_SHOW_RATIONALE_FOR_ALLOW_ALWAYS). You can find an example in the SoniTalk Demo app.

For more information, please see the Javadoc.

## Documentation
You can find more details about how to use each function in the Javadoc.
For information on the SDK design, please see the Developer documentation.

## Specification
The working document of SoniTalk protocol specification is available here: [SoniTalk protocol specification](https://sonitalk.fhstp.ac.at/wp-content/uploads/documentation/draft-zeppelzauer-data-over-sound-00.txt)

### Permission system
SoniTalk includes a built in permission and notification system that ensures the user always knows if an app is currently receiving/sending data via SoniTalk. Depending on the privacy level chosen by the user, permission will be granted for one communication at a time or for longer.

There are three privacy levels:
* Low - Allow always (called Level 0 or L0)
→ The user will be prompted with an Android permission dialog the first time the app tries to use SoniTalk.
* Moderate - Allow until next start of app (called Level 1 or L1)
→ The user will be prompted with a custom dialog after each app start when trying to use SoniTalk.
* Strict - Ask on each communication request (called Level 2 or L2)
→ The user will be prompted with a custom dialog for each communication request via SoniTalk.

Notes:
* Level 0 is implemented by a custom Android permission, otherwise it could not be revoked any more by the user.
* Levels 1 and 2 are handled internally, their dialog offers the option to change the privacy level.

![SoniTalk permission system flow chart](https://sonitalk.fhstp.ac.at/wp-content/uploads/documentation/SoniTalk_Permission_System.PNG)


## Contributing
Please feel free to open Issues, submit Pull Requests, or just send us feedback at sonitalk@fhstp.ac.at

### Known problems
* Duplicate permission problem when two instances of the library exist on one phone.

### Open topics / Features to add
* Add action on notification tap (opening the dev app, or SoniTalk settings ?)
* Add other type of encodings
* Add error correction
* Implement on other platforms (e.g. iOS, Javascript)

## Authors
See the list of [contributors](https://sonitalk.fhstp.ac.at/#team) who participated in this project.

## License
* The SoniTalk SDK is licensed under the GNU Lesser General Public License version 3 (LGPL v3) - see the [LGPL.txt](lgpl.txt) file for details.
* The SoniTalk demonstrator app is licensed under the GNU General Public License version 3 (GPL v3) - see the [GPL.txt](gpl.txt) file for details.
* This document is distributed under CC-BY-Sharelike-3.0 AT

## Acknowledgments
* Project funded by [netidee](https://www.netidee.at/)
* SoniTalk is a project of the Institute for Creative\Media/Technologies [(IC\M/T)](https://icmt.fhstp.ac.at), developed at Sankt Pölten University of Applied Sciences [(FHSTP)](https://www.fhstp.ac.at/en)
* Project website: [sonitalk.fhstp.ac.at](https://sonitalk.fhstp.ac.at/)

## Dependencies and credits
* [IIRJ](https://github.com/berndporr/iirj), last visited April 2019
* [MaryTTS Signalproc](https://mvnrepository.com/artifact/de.dfki.mary/marytts-signalproc/5.1.2), last visited April 2019 
* [Apache Commons Lang](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.8.1), last visited April 2019 
* [Hamming Window by Benjamin Elliott](https://github.com/benelliott/spectrogram-android), last visited April 2019 