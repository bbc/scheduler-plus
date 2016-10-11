# Scheduler Plus

Scheduler Plus is a workflow engine layer using 
[Akka Scheduler](http://doc.akka.io/docs/akka/snapshot/java/scheduler.html) 
and a [Redis](http://redis.io/) database instance that provides a 
cloud-oriented and fault-tolerant mechanism for scheduling work that 
must be repeated at intervals, such as obtaining an object from an 
upstream datasource and adding it to a cache. You can then return the 
lifetime of this object (for example the time from a `max-age` HTTP 
response header for a service you have called) and the system will 
re-execute the work again when that duration has elapsed, i.e. when the 
item should no longer be cached. However, in practice, this could be any 
time and is just when your job should be next scheduled for.

## Using Scheduler Plus

To use Scheduler Plus you need to create your callback for the type of
job you are interested in. A job is how Scheduler Plus tracks work and 
is a response to a job request, which is covered later.
 
Firstly, to create your callback (the work you want to do) you'll need 
to extend the `Callbacks` trait, like:

```scala
import bbc.schedulerplus.client.Callbacks

object TheCallbacks extends Callbacks {
  
  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "the_job_type" => () => {
        
        // do your work here
        
        val someTimeInMillis = 10000
        
        CallbackResponse(lifetimeInMillis = someTimeInMillis)
      }
    }
  }

  def keys: scala.List[String] = List("the_job_type")
}
```

You'll notice there is a ``job.`type` `` match... which returns an 
anonymous function, like:

```scala
() => {
    // your stuff
    val someTimeInMillis = 10000
    
    CallbackResponse(lifetimeInMillis = someTimeInMillis)
}
```

...which forms an enclosure around the work you want to perform and 
returns a `CallbackResponse` which contains a `lifetimeInMillis` which
is when you'd like this job to run again. In practice the job runs near
to the time you ask for, with some entropy to spread system load and
avoid 'lumping' of requests.

Strictly-speaking you don't need the match statement and you could 
return the anonymous function regardless of the ``job.`type` `` but 
it's good practice to check the job type first before you return a 
callback for it (note that it has backticks around the type keyword). 
You also must add the job type to the `keys` function list too so the 
system knows you will deal with that particular job type, otherwise 
Scheduler Plus will never deliver jobs to you, regardless of what 
callbacks you have in the `callbackFor` function. The anonymous function 
returned by `callbackFor` will be executed when Scheduler Plus finds a 
matching job request.

Then to start up the system you can use an `ask`:


```scala
val scheduler = system.actorOf(Props[SchedulerPlusActor], "scheduler-actor")
val response = scheduler ? TheCallbacks
```

...or, if you don't want to know the response, using a `tell`:

```scala
system.actorOf(Props[SchedulerPlusActor], "scheduler-actor") ! TheCallbacks
```

The system will then start up and monitor the cache for any job requests. 
At this stage the system is not doing any work, other than polling the 
cache for job requests.

## Creating Job Requests

To actually run your task, a job request must be created in the cache 
that Scheduler Plus is listening to. This can be done, in redis-cli for 
example, by using:

```
SET bbc.schedulerplus.request:the_job_type_123456 "id=123456|type=the_job_type|status=live"
```

...or your code could add this message to the cache to trigger a job.

Soon, Scheduler Plus will find this request and will schedule it. 
It will create a job in the cache with a similar key, which is used to 
ensure the job isn't lost if the system crashes and also that only one 
Scheduler Plus instance is running this particular job. 

When scheduling the job request, Scheduler Plus will look for a callback 
(in your callbacks) for the `the_job_type` type and store that in the 
job to be executed when the job runs.

## Job Request Statuses

Requests can have the following statuses: `live`, `paused`, and 
`cancelled.` Live requests execute the callback then are re-scheduled 
again. Paused requests' callbacks do not execute but the requests are 
re-scheduled again. Cancelled jobs are ignored. Cancelled jobs are 
equivalent to not having jobs at all, but are explicit and will remove 
any matching job in the cache. In practice you'd probably just remove 
the job request from the cache but the system will attempt to delete any 
jobs relating to cancelled job requests which can help keep the cache 
tidy.

Job requests can be set to any status at any time, by overwriting the 
job request in the cache, say with:

```
SET bbc.schedulerplus.request:the_job_type_123456 "id=123456|type=the_job_type|status=paused"
```

A request which has been scheduled but hasn't yet been executed, could
have the status of `live`. However if the status was changed from `live` 
to `paused` before the job callback actually executes then it will act 
like a paused job and not actually execute. This is so that you can 
pause requests, including ones that are scheduled and currently in 
memory.

## Running

Scheduler Plus is published (currently locally) with:

```
sbt publish
```

...which will build a jar to include in your project dependencies at:
 
```
{project_directory}/bbc/scheduler-plus_2.11-0.1-SNAPSHOT.jar
```

Tests are run with:

```
sbt test
```

Scalastyle with:

```
sbt scalastyle
```

Scoverage test coverage with:

```
sbt clean coverage test
```

Generate Scoverage test report with:

```
sbt coverageReport
```

## About Scheduler Plus

Scheduler Plus came from a subsystem in the Radio and Music Services 
'Blur - Business Layer for Radio' service called 'Scheduler'. Originally 
designed to asynchronously request data and build items from upstream 
services, it can actually be used to execute any code.

## Caveat

Scheduler Plus is a brand new component so under fairly heavy 
development. Stay tuned!

![&#039;Man Plus&#039;](http://i.imgur.com/IrJFFJ4.jpg)
_Lister from Red Dwarf as 'Man Plus'_