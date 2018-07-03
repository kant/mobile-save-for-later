package com.gu.sfl.controller

import com.gu.sfl.lambda.LambdaResponse
import com.gu.sfl.lib.Jackson.mapper
import com.gu.sfl.model._
import com.gu.sfl.util.StatusCodes

trait SaveForLaterController {
  val missingUserResponse = LambdaResponse(StatusCodes.forbidden, Some(mapper.writeValueAsString(ErrorResponse(List(Error("Access Denied", "Access Denied"))))))
  val missingAccessTokenResponse = LambdaResponse(StatusCodes.badRequest, Some("could not find an access token."))
  val identityErrorResponse = LambdaResponse(StatusCodes.internalServerError, Some("Could not retrieve user id."))
  val serverErrorResponse = LambdaResponse(StatusCodes.internalServerError, Some("Server error."))
  val accessDenied = LambdaResponse(StatusCodes.forbidden, Some("Access denied"))
  val emptyArticlesResponse = LambdaResponse(StatusCodes.ok, Some(mapper.writeValueAsString(SavedArticles(List.empty))))
  def maximumSavedArticlesErrorResponse(exception: Exception) = LambdaResponse(StatusCodes.entityToLarge, Some(mapper.writeValueAsString(ErrorResponse(List(Error("Payload too large", exception.getMessage))))))
  def okSyncedPrefsResponse(syncedPrefs: SyncedPrefs): LambdaResponse = LambdaResponse(StatusCodes.ok, Some(mapper.writeValueAsString(SyncedPrefsResponse("ok", syncedPrefs))))
  def okSavedArticlesResponse(savedArticles: SavedArticles): LambdaResponse = LambdaResponse(StatusCodes.ok, Some(mapper.writeValueAsString(SavedArticlesResponse("ok", savedArticles))))
}
