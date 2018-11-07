# Oneday

Have you ever said "one day we ought to do _thing_"?

Or, "someone could do _thing_ in one day"?

## Recognising technical excellence

How do we recognise and reward technical excellence?  Oneday is
"like BountySource.com but for the apps we write at Simply Business"

* Found some area of our codebase that needs a little love and
  attention?  Write up a proposal and attach a Kudosh
  reward.

* Browse through other people's proposals to find something different
  or interesting to pick up.


_[ this next bit is a description of how things will be and has not
yet been implemented ]_

## Posting a proposal

1. Give it a title and description. Make it sound interesting and
attractive so that developers will want to pick it up.  We expect that
small well-defined proposals (*something you can do in One Day*) will
be more successful than large, open-ended or vague ones.

2. Estimate its complexity, Cynefin-style.  If it's Complex or Chaotic
you may no get many takers

3. Decide how important it is and how much you value it.

4. Post it. For great justice!

Optionally you might want to link to Trello cards or Github issues in
your description for background.  That's cool, if the description
itself (or the reward) is compelling enough that people are going to
click through.  You might also want to link from the project source
code to the Oneday page so that anyone looking at that part of the
codebase will find your offer of reward-for-refactoring.

## Browsing

You can see what's previously been offered, comment, and even add your
own rewards.  If you find one you want to work on, add a comment and
check the "I am interested in working on this" sign.  Nothing stops
you from working on the same proposal as somebody else, but it wil be
up to you and them and the sponsors collectively to co-ordinate and to
decide how to split the rewards.

## Claim credit

If you've done the work you deserve the recognition.  To tell the
world and claim the reward, click on the medal icon, and write a note
which includes a link (e.g. the branch or PR URL) to your work. This
will notify the sponsors to go and look: it is then up to you and them
to decide whether the work meets the requirement, and award the
points.

# Development

## Setup

You will need to have a Postgresql instance running.  There is a Procfile
to help with this if you want to run Foreman, but you need to run `initdb`
before it starts:

    initdb -D var/postgresql
    createuser onedayuser
    createdb oneday

## How to

At the beginning of each development session, just run

     $ foreman start
     $ lein migratus migrate

Then run

     $ lein repl
     ondeday.core> (oneday.http/start {})

to get a REPL (interactive top level like the Rails console) and start
the HTTP server on port 3000


# Installation

This bit has yet to be written, but there's going to be a Dockerfile and stuff
