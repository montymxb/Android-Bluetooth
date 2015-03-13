# Android-Bluetooth
Library for simple bluetooth networking on Android (Pre-Bluetooth Low Energy)

<br><br>This is a simple java library for communication over bluetooth in android applications. This library handles finding, connecting, and talking to a client/server over bluetooth. This is an Android Studio library project.

<b>This library is built using pre-ble (bluetooth low energy) api's</b> and will run on API 15, but can run as low as 10 provided you're willing to do without a few features. Connection state is also managed for you, allowing you to tell once a server/client has disconnected from you without a fuss. 

<b>Issues such as dropped packets or temporary instabilities in connection are handled by a simple replay system</b>. If no reply is heard after x amount of time the library will automatically rebroadcast that message, as well as any others you have requested to be sent in addition at that time.

<b>Direct Read/Write to the IOStream is performed from within the library</b>. Your calling application never needs to write/read from the IOStream directly. 

<b>Messages are tagged and may only be fetched with a request containing a matching tag</b>. Instead of reading the raw messages resulting from the stream, you request messages matching a passed in tag. In this way parts of your application can send & collect their own 'baggage' without worrying about anything else. Data being sent in a game could be tagged under "move_updates" for movement related data, score updates tagged under "score_updates", health tagged under "health_updates", etc. The library handles binding data to the given tag, sending it, and then holding it on the other end until a call is made to retrieve a message with that tag.

<br><br>
TODO! I will put up implementation instructions as far as incorporating and utilizing this library. In the meantime if you're not wanting to wait feel free to browse the src and figure it out for yourself!
<br><br>

Originally this was designed for an android application of mine, <a href="https://play.google.com/store/apps/details?id=com.uphouseworks.hangman.paid">Hangman Azul</a>. I eventually started trying to make this library as open ended as possible in order to incorporate it into future projects and to allow others to use it in their own apps as well.

<br>
<i>Please note</i> this project needs some work still. If you'd like to contribute and think you could really help this project blossom feel free to fork it and get back to me with a pull request. Again, there's a lot that needs to be done so I'm sure even a little bit of work would go a long way.
