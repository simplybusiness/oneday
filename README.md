# Boost

FIXME: description

## Installation

Download from http://example.com/FIXME.

## Development Setup

You will need to have a Postgresql instance running.  There is a Procfile
to help with this if you want to run Foreman, but you need to run `initdb`
before it starts

    initdb -D var/postgresql
    createuser boostuser
    createdb boostdb

Then at the beginning of each development session, just run

     foreman start




## Usage

FIXME: explanation

    $ java -jar boost-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
