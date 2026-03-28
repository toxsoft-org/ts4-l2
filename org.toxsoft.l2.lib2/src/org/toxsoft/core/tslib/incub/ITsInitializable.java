package org.toxsoft.core.tslib.incub;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;

/**
 * Mix-in interface of an object that needs additional initialization after constructor.
 * <p>
 * This interface means that it is necessary the method {@link #init(ITsContextRo)} to be called immediately after
 * constructor. There may be several reasons for this kind of two-step initialization:
 * <ul>
 * <li>the object is one of the services in service based application. Generally, service needs other service instances
 * to be created in order to finish own initialization. In such case services manager creates all instances and than
 * calls {@link #init(ITsContextRo)} of created instances;</li>
 * <li>the instance is created by some container that supports dependency injection. To complete initialization injected
 * field values are needed. Usually @PostConstruct annotation is used but sometimes {@link #init(ITsContextRo)} may be
 * preferable;</li>
 * <li>the object is pair of base class and subclass and base implementation needs to call overridden subclass method
 * for initialization to be completed. However it is impossible to call subclass method from base class
 * constructor.</li>
 * </ul>
 * Often {@link ICloseable} or {@link IWorkerComponent} interface is used together with this interface.
 *
 * @author hazard157
 */
public interface ITsInitializable {

  /**
   * Initializes the component.
   *
   * @param aArgs {@link ITsContextRo} - the initialization arguments
   * @return {@link ValidationResult} - determines the success of the initialization
   */
  ValidationResult init( ITsContextRo aArgs );

}
