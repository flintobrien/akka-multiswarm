package com.hungrylearner.pso.util

import scala.reflect.ClassTag

object VectorArray {
  import Numeric.Implicits._

  def tabulate[T: ClassTag: Numeric]( length: Int)(action: Int => T): VectorArray[T] = {
    val v = Array.tabulate[T]( length)(action)
    new VectorArray[T]( v)
  }

  def fill[T: ClassTag: Numeric]( length: Int)(action: => T): VectorArray[T] = {
    val v = Array.fill[T]( length)(action)
    new VectorArray[T]( v)
  }

  def dotAction(v1: List[Double], v2: List[Double])(action:(Double, Double) => Double): List[Double] =
    v1.zip(v2).map {case (x,y) => action(x,y)}

}

/**
 *
 * Mutable vector with standard vector operations.
 *
 * Vector[Double] is very slow due to unboxing. Let's try generic Array.
 *
 * Created by flint on 5/8/14.
 */
class VectorArray[T: ClassTag: Numeric]( protected val v: Array[T]) {
  import Numeric.Implicits._

  def this( _length: Int) = this( new Array[T]( _length))

  def apply( i: Int) = v.apply(i)
  def +=( scalar: T) = for ( i <- v.indices) v(i) += scalar
  def -=( scalar: T) = for ( i <- v.indices) v(i) -= scalar
  def *=( scalar: T) = for ( i <- v.indices) v(i) *= scalar

  def +=( other: VectorArray[T]) = for ( i <- v.indices) v(i) += other(i)
  def -=( other: VectorArray[T]) = for ( i <- v.indices) v(i) -= other(i)
  def *=( other: VectorArray[T]) = for ( i <- v.indices) v(i) *= other(i)

  def +( scalar: T): VectorArray[T]  = new VectorArray( v map { _ + scalar})
  def -( scalar: T): VectorArray[T]  = new VectorArray( v map { _ - scalar})
  def *( scalar: T): VectorArray[T]  = new VectorArray( v map { _ * scalar})

  def +( other: VectorArray[T]): VectorArray[T]  = new VectorArray( v zip other.v map { case (a, b) => a + b})
  def -( other: VectorArray[T]): VectorArray[T]  = new VectorArray( v zip other.v map { case (a, b) => a - b})
  def *( other: VectorArray[T]): VectorArray[T]  = new VectorArray( v zip other.v map { case (a, b) => a * b})

  def sum = v.sum

  def foreach( f: (T) => Unit): Unit = v.foreach( f)
  def applyToAll( f: T => T): Unit = for ( i <- v.indices) { v(i) = f(v(i))}

//  def map[T: ClassTag: Numeric](action: T => T): ArrayVector[T] = {
//    val v2 = v.map( s => action(s))
//    new ArrayVector[T]( v2)
//  }

}

