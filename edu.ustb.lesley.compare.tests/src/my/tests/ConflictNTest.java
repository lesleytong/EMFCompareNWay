/**
 */
package my.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import my.ConflictN;
import my.MyFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Conflict N</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ConflictNTest extends TestCase {

	/**
	 * The fixture for this Conflict N test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ConflictN fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ConflictNTest.class);
	}

	/**
	 * Constructs a new Conflict N test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConflictNTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Conflict N test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(ConflictN fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Conflict N test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ConflictN getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(MyFactory.eINSTANCE.createConflictN());
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

} //ConflictNTest
