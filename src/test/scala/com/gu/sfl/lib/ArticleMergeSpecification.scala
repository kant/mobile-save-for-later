package com.gu.sfl.lib

import java.time.LocalDateTime

import com.gu.sfl.exception.{MaxSavedArticleTransgressionError, SavedArticleMergeError}
import com.gu.sfl.model.{SavedArticle, SavedArticles, SyncedPrefs}
import com.gu.sfl.persisitence.SavedArticlesPersistenceImpl
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.util.{Failure, Success}

class ArticleMergeSpecification extends Specification with Mockito  {

  "updateSavedArticlesWithRetryAndMerge" should {
    val userId = "123"
    val version = "1"

    val (article1, article2, article3, article4) = (
      SavedArticle("id/1", "p/1", LocalDateTime.of(2018, 1, 16, 16, 30), read = true),
      SavedArticle("id/2", "p/2", LocalDateTime.of(2018, 2, 17, 17, 30), read = false),
      SavedArticle("id/3", "p/3", LocalDateTime.of(2018, 3, 18, 18, 30), read = true),
      SavedArticle("id/4", "p/4", LocalDateTime.of(2018, 4, 19, 19, 30), read = true)
    )

    val savedArticles = SavedArticles(version, List(article1, article2))
    val savedArticlesUpdate1 = SavedArticles(version, List(article3))
    val savedArticles2 = SavedArticles(version, List(article1, article2, article3))

    "saves the articles if the user does not currently have any articles saved" in new Setup {
      val responseArticles = Success(Some(savedArticles.advanceVersion))
      val expectedMergeResponse = Success(Some(savedArticles.advanceVersion))

      savedArticlesPersistence.read(userId) returns (Success(None))
      savedArticlesPersistence.write(userId, savedArticles) returns (responseArticles)
      val saved = savedArticlesMerger.updateWithRetryAndMerge(userId, savedArticles)
      there was one(savedArticlesPersistence).read(userId)

      there was no(savedArticlesPersistence).update(Mockito.any[String](), Mockito.any[SavedArticles]())
      there were no(savedArticlesPersistence).update(userId, savedArticles)
      saved shouldEqual (expectedMergeResponse)
    }

    "will update the the users' saved articles if there is no conflict" in new Setup {
      val responseArticles = Success(Some(savedArticles2.advanceVersion))
      val expectedMergeResponse = Success(Some(savedArticles2.advanceVersion))

      savedArticlesPersistence.read(userId) returns(Success(Some(savedArticles)))
      savedArticlesPersistence.update(argThat(===(userId)), argThat(===(savedArticles2))) returns(responseArticles)
      val saved = savedArticlesMerger.updateWithRetryAndMerge(userId, savedArticles2)
      there was one(savedArticlesPersistence).read(argThat(===(userId)))
      there were no(savedArticlesPersistence).write(any[String](), any[SavedArticles]())
      there was one(savedArticlesPersistence).update(argThat(===(userId)), argThat(===(savedArticles2)))
      saved shouldEqual (expectedMergeResponse)
    }
    

    "will not try to merge a list of articles with a length greater than the saved article limit" in new Setup {
      private val articleSaveLimit = 2
      override val savedArticlesMerger = new SavedArticlesMergerImpl(SavedArticlesMergerConfig(articleSaveLimit), savedArticlesPersistence)
      val saved = savedArticlesMerger.updateWithRetryAndMerge(userId, savedArticles2)
      there were no (savedArticlesPersistence).read(argThat(===(userId)))
      there were no (savedArticlesPersistence).write(any[String](), any[SavedArticles]())
      there were no (savedArticlesPersistence).update(any[String](), any[SavedArticles]())
      saved mustEqual(Failure(MaxSavedArticleTransgressionError(s"Tried to save more than $articleSaveLimit articles.")))
    }

    "failure to get current articles throws the correct exception" in new Setup {
      savedArticlesPersistence.read(userId) returns (Failure(new IllegalStateException("Bad, bad, bad")))
      val saved = savedArticlesMerger.updateWithRetryAndMerge(userId, savedArticles)
      there was one(savedArticlesPersistence).read(argThat(===(userId)))
      saved shouldEqual (Failure(SavedArticleMergeError("Could not retrieve current articles")))
    }

    "failure to update the saved articles results in the currect error" in new Setup {
      savedArticlesPersistence.read(userId) returns (Success(None))
      savedArticlesPersistence.write(userId, savedArticles) returns (Failure(new IllegalStateException("My mummy told me to be good, but I was naughty")))

      val saved = savedArticlesMerger.updateWithRetryAndMerge(userId, savedArticles)
      saved shouldEqual (Failure(SavedArticleMergeError("Could not update articles")))
    }
  }

  trait Setup extends Scope {
    val savedArticlesPersistence = mock[SavedArticlesPersistenceImpl]
    val savedArticlesMerger = new SavedArticlesMergerImpl(SavedArticlesMergerConfig(20), savedArticlesPersistence)
  }

}
