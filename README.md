# Wilson
Microservice case study for Reddit-style voting.
Ranking is based on [How Not to Sort by Average Rating][not-average].

It follows the [Twelve-Factor App][12-factor-app] recommendations.

## Prerequisites
For running the application you will need [Docker] (tested with 1.12.1) and Docker Compose (tested with 1.8.0).

## Running
To start the service together with a database, run:

    docker-compose up

## Setup
Before interacting with the service for the first time the database tables have to be set up.
Assuming the containers are running, this is done via

    docker exec -it wilson_wilson_1 lein setup

Note that `wilson_wilson_1` is a name assigned by Docker Compose and could be different. The names of running containers can be obtained via `docker ps`.

## API
After starting the service you can read the [Swagger] API documentation at [localhost:3000/api-docs](http://localhost:3000/api-docs).

## Development
The application is written in Clojure. The API is powered by Ring, Compojure and [`compojure-api`][compojure-api].

### Prerequisites
For development you will need to have [Leiningen] (tested with 2.6.1) and Java (tested with 1.8.0_60) installed.

### Running
Running the application via Leiningen assumes a RethinkDB instance is running on `localhost:28015`.

    lein ring server

You can start a RethinkDB instance via Docker:

    docker run -p 8080:8080 -p 28015:28015 -p 29015:29015 -v "$PWD:/data" rethinkdb

#### Database URL
You can configure the RethinkDB location via the `DB_URL` environment variable, e.g.
`DB_URL="//localhost:28015"`
Note that the string has to be parseable by `java.net.URI`.

### Setup
Setup is done once via

    lein setup

#### Database Name
You can specify a database name by setting the environment variable `DB_NAME`.
RethinkDB rules apply, so use `A-Za-z0-9_` only.

In development it defaults to `test`, as specified in `project.clj`, while running the service via Docker Compose will default to `wilson` as specified in `docker-compose.yml`.

### Tests
Tests are written with `clojure.test` and can be run via

    lein test

[not-average]: http://www.evanmiller.org/how-not-to-sort-by-average-rating.html
[12-factor-app]: https://12factor.net/
[leiningen]: https://github.com/technomancy/leiningen
[docker]: https://www.docker.com/
[swagger]: http://swagger.io/
[compojure-api]: https://github.com/metosin/compojure-api
