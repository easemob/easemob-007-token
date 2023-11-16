# easemob-007-token

## Overview

This demo is a Java project that demonstrates how to register users on the Agora Chat server in bulk and how to retrieve an Agora Chat app token and user token for login to use the chat service.

### Development environment

- JDK1.8
- Spring Boot(2.4.3)

### Configuration

Set the following parameters in `src/main/resources/application.properties`:

```
## Server port.
server.port=8082
## App ID of your Agora project.
appid=
## App certificate of your Agora project.
appcert=
## Token validity period in the range of 1 to 86400 in seconds.
expire.second=
## App key of the Agora Chat service in the format of xxx#xxx.
appkey=
## REST API domain for the Agora Chat service, example:xxx.chat.agora.io
domain=
```

### Function

- Retrieves an Agora Chat app token. For use in calling RESTful API.
- Retrieves an Agora Chat user token. For client login to Agora chat server usage.
- Registers the Agora Chat user in bulk. 

### API：

### Retrieves an Agora Chat app token

**Path:** `http://localhost:8082/chat/app/token`

**HTTP Method:** `GET`

**Request Example:**
```
curl -X GET http://localhost:8082/chat/app/token
```

**Response Example:**
```
{
    "result": "007eJxTYKgNuZ0t7+kzTf9+UEexd7HlXXXXXBlYGRgYkBxGdgAADFah9U"
}
```

### Retrieves an Agora Chat user token

**Path:** `http://localhost:8082/chat/user/{chatUserName}/token`

**HTTP Method:** `GET`

**Request Example:**
```
curl -X GET http://localhost:8082/chat/user/tom/token
```

**Response Example:**
```
{
    "result": "007klMnbfuZ0t7+kzTf9+UEexd7HlXXXXXBlYGRgYkBxKLOP8KM"
}
```

### Registers the Agora Chat user in bulk

**Path:** `http://localhost:8082/chat/user/register`

**HTTP Method:** `POST`

**Request Body:**

| Field | Type | Description    |
| --- | --- |----------------|
| usernames  | Array | List of usernames to register |

**Request Example:**
```
curl -X POST http://localhost:8082/chat/user/register -H 'Content-Type: application/json' -d '{"usernames" : ["tom", "jack"]}'
```

**Response Example:**
```
{
    "result": "register successful"
}
```
