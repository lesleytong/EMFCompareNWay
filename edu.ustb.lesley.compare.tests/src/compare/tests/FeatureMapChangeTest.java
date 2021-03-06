/**
 */
package compare.tests;

import compare.CompareFactory;
import compare.FeatureMapChange;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Feature Map Change</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class FeatureMapChangeTest extends DiffTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(FeatureMapChangeTest.class);
	}

	/**
	 * Constructs a new Feature Map Change test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FeatureMapChangeTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Feature Map Change test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected FeatureMapChange getFixture() {
		return (FeatureMapChange)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(CompareFactory.eINSTANCE.createFeatureMapChange());
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

} //FeatureMapChangeTest
