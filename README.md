# EkkoChat Server

EkkoChat is my implementation of a secure chat application using Java.

I am using Spring Modulith for building the backend in a scalable way but still maintaining it as a single codebase.

# Features

## Standard chat

The standard chat mode in this application will be where the messages are only encrypted on the server for storage.

The encryption key will be provided via an environment variable.

I could also build a separate spring batch project which takes 2 args, the old key and the new key, and does the migration of encryption keys in a batch job.

## E2EE chat

The end-to-end encrypted chat mode is where the server doesn't handle any of the message encryption itself, the client does all of this.

When a user wants to register for E2EE chats, they create a public & private keypair, then create a cryptographically secure symmetric key called the Key encryption key (KEK).

The KEK will then be encrypted with a key derived from the users' current password (PDK), this way, the user can reset their password, which only has to re-encrypt the KEK with the new one, without this, the old messages would be unreadable after a password reset.

Finally, the user sends their plaintext public key, encrypted private key, encrypted KEK, and any encryption parameters, like the KDF salt generated for the PDK.

## Notifications

I'm thinking of starting with internal notifications using websockets first, then maybe see about push notifications later.

