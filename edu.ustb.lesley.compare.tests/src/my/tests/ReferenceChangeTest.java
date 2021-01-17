/**
 */
package my.tests;

import junit.textui.TestRunner;

import my.MyFactory;
import my.ReferenceChange;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Reference Change</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ReferenceChangeTest extends DiffNTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ReferenceChangeTest.class);
	}

	/**
	 * Constructs a new Reference Change test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReferenceChangeTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Reference Change test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected ReferenceChange getFixture() {
		return (ReferenceChange)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(MyFactory.eINSTANCE.createReferenceChange());
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

} //ReferenceChangeTest
