package org.toxsoft.l2.lib.api;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * L2 Application specific error.
 * <p>
 * Use this exception if there is no suitable common tslib TsXxxException or USKat SkXxxException.
 *
 * @author hazard157
 */
public class L2RtException
    extends TsRuntimeException {

  private static final String LOG_STR_ERR_L2_APP_ERROR = "L2 Application specific error occured"; //$NON-NLS-1$

  private static final long serialVersionUID = 157157L;

  /**
   * Constructor for wrapper exception.
   * <p>
   * Message string is created using {@link String#format(String, Object...)}.
   *
   * @param aCause Throwable - cause, mey be <code>null</code>
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   */
  public L2RtException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }

  /**
   * Constructor.
   * <p>
   * Message string is created using {@link String#format(String, Object...)}.
   *
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   */
  public L2RtException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
  }

  /**
   * Constructor for wrapper exception with preset message.
   *
   * @param aCause Throwable - cause, mey be <code>null</code>
   */
  public L2RtException( Throwable aCause ) {
    super( LOG_STR_ERR_L2_APP_ERROR, aCause );
  }

  /**
   * Constructor with preset message.
   */
  public L2RtException() {
    super( LOG_STR_ERR_L2_APP_ERROR );
  }

  // ------------------------------------------------------------------------------------
  // Check & throw methods
  //

  /**
   * Throws exception if condition is <code>false</code>.
   *
   * @param aCondition boolean - checked condition
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   * @throws L2RtException aCondition == <code>false</code>
   */
  public static void checkFalse( boolean aCondition, String aMessageFormat, Object... aMsgArgs )
      throws L2RtException {
    if( !aCondition ) {
      throw new L2RtException( aMessageFormat, aMsgArgs );
    }
  }

  /**
   * Throws an exception with preset message if condition is <code>false</code>.
   *
   * @param aCondition boolean - checked condition
   * @throws L2RtException aCondition == <code>false</code>
   */
  public static void checkFalse( boolean aCondition )
      throws L2RtException {
    if( !aCondition ) {
      throw new L2RtException( LOG_STR_ERR_L2_APP_ERROR );
    }
  }

  /**
   * Throws an exception if condition is <code>true</code>.
   *
   * @param aCondition boolean - checked condition
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   * @throws L2RtException aCondition == <code>true</code>
   */
  public static void checkTrue( boolean aCondition, String aMessageFormat, Object... aMsgArgs )
      throws L2RtException {
    if( aCondition ) {
      throw new L2RtException( aMessageFormat, aMsgArgs );
    }
  }

  /**
   * Throws an exception with preset message if condition is <code>true</code>.
   *
   * @param aCondition boolean - checked condition
   * @throws L2RtException aCondition == <code>true</code>
   */
  public static void checkTrue( boolean aCondition )
      throws L2RtException {
    if( aCondition ) {
      throw new L2RtException( LOG_STR_ERR_L2_APP_ERROR );
    }
  }

  /**
   * Throws an exception if reference is <code>null</code>.
   *
   * @param <E> - reference type
   * @param aRef &lt;E&gt; - checked reference
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   * @return &lt;E&gt; - always returns aRef
   * @throws L2RtException aRef == <code>null</code>
   */
  public static <E> E checkNull( E aRef, String aMessageFormat, Object... aMsgArgs )
      throws L2RtException {
    if( aRef == null ) {
      throw new L2RtException( aMessageFormat, aMsgArgs );
    }
    return aRef;
  }

  /**
   * Throws an exception with preset message if reference is <code>null</code>.
   *
   * @param <E> - reference type
   * @param aRef &lt;E&gt; - checked reference
   * @return &lt;E&gt; - always returns aRef
   * @throws L2RtException aRef == <code>null</code>
   */
  public static <E> E checkNull( E aRef )
      throws L2RtException {
    if( aRef == null ) {
      throw new L2RtException( LOG_STR_ERR_L2_APP_ERROR );
    }
    return aRef;
  }

  /**
   * Throws an exception if reference is <b>not</b> <code>null</code>.
   *
   * @param <E> - reference type
   * @param aRef &lt;E&gt; - checked reference
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   * @return &lt;E&gt; - always returns aRef
   * @throws L2RtException aRef != <code>null</code>
   */
  public static <E> E checkNoNull( E aRef, String aMessageFormat, Object... aMsgArgs )
      throws L2RtException {
    if( aRef != null ) {
      throw new L2RtException( aMessageFormat, aMsgArgs );
    }
    return aRef;
  }

  /**
   * Throws an exception with preset message if reference is <b>not</b> <code>null</code>.
   *
   * @param <E> - reference type
   * @param aRef &lt;E&gt; - checked reference
   * @return &lt;E&gt; - always returns aRef
   * @throws L2RtException aRef != <code>null</code>
   */
  public static <E> E checkNoNull( E aRef )
      throws L2RtException {
    if( aRef != null ) {
      throw new L2RtException( LOG_STR_ERR_L2_APP_ERROR );
    }
    return aRef;
  }

}
