# Scheduler Plus

Scheduler Plus is a workflow engine layer on Akka Scheduler and provides 
a cloud-oriented and fault-tolerant mechanism for scheduling work that 
must be repeated at intervals, such as obtaining an object from an 
upstream datasource and adding it to a cache. You can then return the 
lifetime of this object (for example the time from a `max-age` HTTP 
response header for a service you have called) and the system will 
re-execute the work again when that duration has elapsed, i.e. when 
the item should no longer be cached. However, in practice, this could
be any time and is just when your job should be next scheduled for.

## Using Scheduler Plus

To use Scheduler Plus you need to create your callback for the type of
job you are interested in. A job is how Scheduler Plus tracks work and 
is a response to a job request, which is covered later.
 
Firstly, to create your callback (the work you want to do) you'll need 
to extend the `Callbacks` trait, like:

```scala
import bbc.persistence.sync.Callbacks

object HelloWorldCallbacks extends Callbacks {
  
  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "the_job_type" => () => {
        
        // do your work here

        CallbackResponse(lifetimeInMillis = 10000)
      }
    }
  }

  def keys: scala.List[String] = List("the_job_type")
}
```

You'll notice there is a `job.``type`` match` which then returns an 
anonymous function, like:

```scala
() => {
    // your stuff
    val someTimeInMillis = 10000
    
    CallbackResponse(lifetimeInMillis = someTimeInMillis)
}
```

Strictly-speaking you don't need the match statement and you could 
return the anonymous function directly but it's good practice to check 
the job type first before you return a callback for it. You also must
add the job type to the `keys` function return too so the system knows
you will deal with that particular job type. 

Scheduler Plus will execute this anonymous function when the job runs.

Then to start up the system you look up the actor and send these 
callbacks to it, using an `ask`:

```scala
val scheduler = system.actorOf(Props[SchedulerPlusActor], "scheduler-actor")
val response = scheduler ? HelloWorldCallbacks
```

...or, if you want to fire-and-forget, using a `tell`:

```scala
system.actorOf(Props[SchedulerPlusActor], "scheduler-actor") ! HelloWorldCallbacks
```

The system will then start up and monitor the cache for any job requests. 
At this stage the system is not doing any work, other than polling the 
cache for job requests.

## Creating Job Requests

To actually run your task, a job request must be created in the cache 
that Scheduler Plus is listening to. This can be done, for Redis, by 
using:

```
SET bbc.persistence.sync.JobRequest:the_job_type_123456 
"{\"type\":\"the_job_type\",\"id\":\"123456\",\"status\":\"live\"}"
```
_(all on one line)_

This adds an item with the key `bbc.persistence.sync.JobRequest:the_job_type_123456` 
and the JSON value of:

```json
{
	"type": "the_job_type",
	"id": "123456",
	"status": "live"
}
```

Soon, Scheduler Plus will find this job request and will start to 
execute it. It will create a job in the cache with a similar key, which 
is used to ensure the job isn't lost if the system crashes and also that 
only one Scheduler Plus instance is running this particular job. 

When scheduling the job request, Scheduler Plus will look for a callback 
(in your callbacks) for the `the_job_type` type and then execute that 
callback. Your callback will then return a `CallbackResponse` with a 
`lifetimeInMillis` and Scheduler Plus will re-schedule your task for as 
close to that time as it can. Scheduler Plus may add some time onto the 
actual execution time to spread system load.

## Job Request Statuses

Job requests can have the following statuses: `live`, `paused`, and 
`cancelled.` Live jobs are executed and then re-scheduled to be executed 
again. Paused jobs do not actually execute but are re-scheduled again. 
Cancelled jobs are ignored. Cancelled jobs are equivalent to not having 
jobs at all, but are explicit. In practice you'd probably just remove 
the job request from the cache but the system will attempt to delete any 
jobs relating to cancelled job requests which can help keep the cache tidy.

Job requests can be set to any status at any time, by overwriting the 
job request in the cache, say with:

```
SET bbc.persistence.sync.JobRequest:the_job_type_123456 
"{\"type\":\"the_job_type\",\"id\":\"123456\",\"status\":\"paused\"}"
```
_(all on one line)_

If a job is currently scheduled but hasn't yet been executed, it may 
have the status of `live` which would mean it would be executed. However 
if the status was changed from `live` to `paused` before the job 
actually executes then it will act like a paused job and not actually 
execute. This is so that you can pause all jobs, including ones that are 
scheduled.

## Running Scheduler Plus

The library contains a 'Hello, World!' example, which (assuming you have 
local Redis installed and running) you can run using:

```
sbt run
```

This is an example only and Scheduler Plus is intended as a library 
module so it should be included in your sbt dependencies as:

```
"bbc.rms" %% "scala-scheduler-plus-lib" % "X.X",
```

...and follow the above instructions to integrate it with your project.

Tests are run with:

```
sbt run
```

...and Scalastyle with:

```
sbt scalastyle
```

## About Scheduler Plus

Scheduler Plus came from a subsystem in the Radio and Music Services 
'Blur - Business Layer for Radio' service. Originally designed to 
asynchronously request data and build items from upstream services it 
can actually be used to execute any task.  

## Caveat 

Scheduler Plus is currently in alpha mode. Email beth.anderson@bbc.co.uk

![&#039;Man Plus&#039;](http://i.imgur.com/IrJFFJ4.jpg)
