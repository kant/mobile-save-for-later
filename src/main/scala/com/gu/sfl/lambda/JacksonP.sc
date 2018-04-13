import java.text.SimpleDateFormat
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.util.{Failure, Success, Try}

val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

val mapper = new ObjectMapper() with ScalaObjectMapper

mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
mapper.setDateFormat(formatter)
mapper.registerModule(DefaultScalaModule)
mapper.registerModule(new Jdk8Module())
mapper.registerModule(new JavaTimeModule())
case class Shitter(idd: String)

case class SavedArticle(id: String, shortUrl: String, date: LocalDateTime, read: Boolean)

//This is cribbed from the current identity model:  https://github.com/guardian/identity/blob/master/identity-model/src/main/scala/com/gu/identity/model/Model.scala
//Todo - eventually we may no longer need the syncedPrefs hierarchy  because at this point its only saving articles which we're interested in
case class SyncedPrefs(userId: String, savedArticles :Option[SavedArticles])  {
  def ordered: SyncedPrefs = copy( savedArticles = savedArticles.map(_.ordered) )
}

sealed trait SyncedPrefsData {
  def version: String
  val nextVersion = SavedArticles.nextVersion()
  def advanceVersion: SyncedPrefsData

}

object SavedArticles {
  def nextVersion() = Instant.now().toEpochMilli.toString
  def apply(articles: List[SavedArticle]) : SavedArticles = SavedArticles(nextVersion(), articles)
}

case class SavedArticles(version: String, articles: List[SavedArticle]) extends SyncedPrefsData {
  override def advanceVersion: SyncedPrefsData = copy(version = nextVersion)
  def ordered: SavedArticles = copy()
}


val d = LocalDateTime.now()
val a = SavedArticle(id = "/uk/politics/corbyn-knobber", date = d, shortUrl = "/p/ksjd2", read = false)


val json = """{
             |  
             |    "version": "1415719219337",
             |    "articles": [
             |      {
             |        "id": "commentisfree/2006/mar/01/whiteteeth",
             |        "shortUrl": "p/abc",
             |        "date": "2014-03-01T11:54:37Z",
             |        "read": true
             |      },
             |      {
             |        "id": "commentisfree/2014/jul/17/guardian-view-assisted-dying-falconer-bill",
             |        "shortUrl": "p/123",
             |        "date": "2014-07-21T09:10:37Z",
             |        "read": false
             |      },
             |      {
             |        "id": "world/2014/nov/06/luxembourg-tax-leaks-put-pressure-on-g20-leaders-to-act-on-loopholes",
             |        "shortUrl": "p/1234",
             |        "date": "2014-11-06T17:27:37Z",
             |        "read": false
             |      }
             |    ]
             |}
             |""".stripMargin


val json2 = """{
             |    "version": "1415719219337",
             |    "articles": [
             |      {
             |        "id": "commentisfree/2006/mar/01/whiteteeth",
             |        "shortUrl": "p/abc",
             |        "date": "2014-03-01T11:54:37Z",
             |        "read": true
             |      }
             |    ]
             |}
             |""".stripMargin



Try(mapper.readValue(json2, classOf[SavedArticles])) recoverWith {
  case t: Throwable => println(s"Caught", t)
  Failure(t)
}