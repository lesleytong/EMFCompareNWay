/**
 */
package my.tests;

import junit.textui.TestRunner;

import my.MyFactory;
import my.ResourceLocationChange;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Resource Location Change</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ResourceLocationChangeTest extends DiffNTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ResourceLocationChangeTest.class);
	}

	/**
	 * Constructs a new Resource Location Change test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceLocationChangeTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Resource Location Change test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected ResourceLocationChange getFixture() {
		return (ResourceLocationChange)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(MyFactory.eINSTANCE.createResourceLocationChange());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

} //ResourceLocationChangeTest
