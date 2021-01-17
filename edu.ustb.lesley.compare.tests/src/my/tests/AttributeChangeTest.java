/**
 */
package my.tests;

import junit.textui.TestRunner;
import my.AttributeChange;
import my.MyFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Attribute Change</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class AttributeChangeTest extends DiffNTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(AttributeChangeTest.class);
	}

	/**
	 * Constructs a new Attribute Change test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AttributeChangeTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Attribute Change test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected AttributeChange getFixture() {
		return (AttributeChange)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(MyFactory.eINSTANCE.createAttributeChange());
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

} //AttributeChangeTest
