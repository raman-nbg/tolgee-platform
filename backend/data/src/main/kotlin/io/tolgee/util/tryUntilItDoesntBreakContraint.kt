package io.tolgee.util

import jakarta.persistence.PersistenceException
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.DataIntegrityViolationException

inline fun <T> tryUntilItDoesntBreakConstraint(fn: () -> T): T {
  var exception: Exception? = null
  var repeats = 0
  for (it in 1..100) {
    try {
      return fn()
    } catch (e: Exception) {
      when (e) {
        is DataIntegrityViolationException, is PersistenceException, is CannotAcquireLockException -> {
          repeats++
          exception = e
        }
        else -> throw e
      }
    }
  }

  throw RepeatedlyThrowingConstraintViolationException(exception!!, repeats)
}

class RepeatedlyThrowingConstraintViolationException(cause: Throwable, repeats: Int) :
  RuntimeException("Retry failed $repeats times.", cause)
