/**
 * L2 library.
 * <p>
 * L2 is an abbreviation from <b>L</b>ow <b>L</b>evel. In the industrial automation systems Low Level is a PLC
 * (controller) level where the process control software is operating. High Level is the SCADA or other software
 * communicating with (several) Low Levels,
 * <p>
 * L2 library introduces the L2 application {@link org.toxsoft.l2.lib.app.IL2Application IL2Application} that may run
 * either as a separate console application started with <code>org.toxsoft.l2.main.L2Main</code> class from the
 * <code>org.toxsoft.l2.main</code> project or as a part of other application. When run from the application (such as
 * USkat server or workstation), several different instances of the {@link org.toxsoft.l2.lib.app.IL2Application
 * IL2Application} may run in the same JVM.
 * <p>
 * <h2>L2 application structure</h2>
 * <p>
 * L2 application operates much like the PLC firmware. Application main thread is an infinite loop (Super-Loop, Main
 * Loop) called <i>sweep</i>:
 * <ol>
 * <li>read inputs - reads data from the physical world;</li>
 * <li>process logic - executes the user code using input values and form the values for output;</li>
 * <li>write outputs - writes values prepared by logic to the physical world;</li>
 * <li>networking - communicates to the High Level, some housekeeping, etc.</li>
 * </ol>
 * <p>
 * L2 has three main <i>components</i> responsible for sweep steps:
 * <ul>
 * <li>HAL (Hardware Abstraction Layer) - contains device drivers communicating with the physical world. HAL is
 * responsible for steps #1 and #3: reading inputs and writing outputs;</li>
 * <li>DLM Manager (Dynamic Loadable Modules manager) - operates with user-supplied DLM implementation of the process
 * logic, the sweep step #2;</li>
 * <li>Network - is responsible for sweep step #4, communication with the USkat server.</li>
 * </ul>
 * <h2>Implementation details</h2>
 * <h3>L2 components</h3>
 * <p>
 * Implementation details of the components are described in the respective component interfaces and packages:
 * <ul>
 * <li>HAL - {@link org.toxsoft.l2.lib.hal.IL2Hal};</li>
 * <li>DLM manager - {@link org.toxsoft.l2.lib.dlms.IL2DlmManager};</li>
 * <li>Network - {@link org.toxsoft.l2.lib.net.IL2Network}.</li>
 * </ul>
 * <p>
 * Below is the brief description of the L2 application commons:
 * <h3>Modules in the components</h3>
 * <p>
 * Each L2 component may load additional modules. For DLM manager it is a DLMs implemented as a JAR-files, for HAL - the
 * device drivers.
 * <h3>Application configuration</h3> <br>
 * There is the two kind of configuration data:
 * <ul>
 * <li><i>global options</i> - application and components configuration is a plain list of options. Each option is a
 * pair of option ID (an IDpath) and option value (an atomic value {@link org.toxsoft.core.tslib.av.IAtomicValue
 * IAtomicValue});</li>
 * <li><i>module configurations</i> - what module to load and the initialization parameters of the module are determined
 * by the <i>module configuration file</i> (MCF), see detail below.</li>
 * </ul>
 * <p>
 * <b>Global options</b><br>
 * Global options are passed at the application initialization
 * {@link org.toxsoft.l2.lib.app.IL2Application#init(org.toxsoft.core.tslib.bricks.ctx.ITsContextRo)
 * IL2Application.init(ITsContextRo)} as a parameters {@link org.toxsoft.core.tslib.bricks.ctx.ITsContextRefDef#params()
 * ITsContextRefDef.params()}. They are declared in the interfaces:
 * {@link org.toxsoft.l2.lib.IL2GlobalOptions#ALL_GLOBAL_OPDEFS IL2GlobalOptions#ALL_GLOBAL_OPDEFS}.
 * <p>
 * It {@link org.toxsoft.l2.lib.impl.L2Application} user responsibility t prepare global option values. In case when
 * application is invoked by the launcher <code>org.toxsoft.l2.main.L2Main</code> the configuration file is used as
 * described in <code>org.toxsoft.l2.main.IL2MainConstants</code>. Is such case global option values is formed initially
 * from the option definitions default values. Then values are applied from the configuration file and then the values
 * from the launcher command line are applied.
 * <p>
 * <b>Module configuration data</b><br>
 * For each component MCFs are placed in the respective subdirectory of the configuration files root directory
 * {@link org.toxsoft.l2.lib.IL2GlobalOptions#OPDEF_L2_COMP_CFG_DIR_ROOT OPDEF_L2_APP_CFG_DIR_ROOT}. The name of the
 * subdirectory is the corresponding {@link org.toxsoft.l2.lib.EL2ComponentKind#id()}. Each file in subdirectory
 * corresponds to the one module to be loaded at runtime. All MCFs has the same format as specified in
 * {@link org.toxsoft.l2.lib.common.L2ModuleConfigFile L2ModuleConfigFile}.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 */
package org.toxsoft.l2.lib;
