# Minimal Chat

Example project using ClojureScript and Firebase as the basis for
creating a nearly entirely javascript based chat client.

## Usage

1. Sign up for a firebase.com account.
1. Make a Firebase application.
1. Clone the project.
1. Change `firebase-io-root` to have the URL for your Firebase
   application.
1. Run `lein figwheel` in the project root to start up a local
   webserver [http://localhost:3449](http://localhost:3449).
1. Go to your browser and start playing around in the code!

## Developing

To start a local server that causes CSS and ClojureScript changes to
be reflected immediately in your browser run `lein fighweel` and open
up [http://localhost:3449](http://localhost:3449)


To run the ClojureScript tests run `lein auto-test`. It will recompile
the ClojureScript and rerun the tests whenever a file changes.

