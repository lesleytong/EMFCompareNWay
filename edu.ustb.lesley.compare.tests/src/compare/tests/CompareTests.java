/**
 */
package compare.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>compare</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class CompareTests extends TestSuite {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static Test suite() {
		TestSuite suite = new CompareTests("compare Tests");
		suite.addTestSuite(ComparisonTest.class);
		suite.addTestSuite(MatchTest.class);
		suite.addTestSuite(DiffTest.class);
		suite.addTestSuite(ResourceAttachmentChangeTest.class);
		suite.addTestSuite(ResourceLocationChangeTest.class);
		suite.addTestSuite(ReferenceChangeTest.class);
		suite.addTestSuite(AttributeChangeTest.class);
		suite.addTestSuite(FeatureMapChangeTest.class);
		suite.addTestSuite(ConflictTest.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CompareTests(String name) {
		super(name);
	}

} //CompareTests
