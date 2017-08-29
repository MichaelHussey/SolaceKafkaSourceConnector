# Kafka Connect Solace Source Connnector

A Kafka Connect *source* which listens to messages on a Solace topic and forwards them to a Kafka topic

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You need both a Solace Message Router and a Kafka environment. 

If you don't have a physical Solace Appliance available to you then use either a Virtual Message Router [VMR](http://dev.solace.com/downloads/) or register on [Solace's messaging as a service](https://datago.io).

In order to connect to the Solace message router you need a number of parameters

Parameter       | Description
--------------- | -------------
solace.smfHost  | ipaddress or host name of the Solace Router. If the SMF interface on the router is not running on the standard port (55555) then append the port number as well e.g. `127.0.0.1:55559`|
solace.msgVpn   | the message VPN to connect to on the Router. Optional, if not provided `default` is used.
solace.username | identity to use to connect to the Router. Optional, if not provided `default` is used.
solace.password | password, may be empty
solace.topic    | The Solace topic to listen on. May contain wildcards, eg `test/>`.

In order to successfully run the Unit Tests for the Connector you need to edit the `src/test/resources/unit_test.properties` file and provide the correct values for your environment.

### Installing

Grab a copy of the project using GIT from XXX

Build using Gradle...

```
./gradlew build
```

...or using Maven

```
mvn compile
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Modify the file [`config/quickstart-solace_source.properties`]

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Mic Hussey** - *Initial work* - [Solace](https://github.com/MichaelHussey)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Resources

For more information try these resources:

- The Solace Developer Portal website at: http://dev.solace.com
- Get a better understanding of [Solace technology](http://dev.solace.com/tech/).
- Check out the [Solace blog](http://dev.solace.com/blog/) for other interesting discussions around Solace technology
- Ask the [Solace community.](http://dev.solace.com/community/)

