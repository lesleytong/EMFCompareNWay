/**
 */
package compare.tests;

import compare.CompareFactory;
import compare.ResourceAttachmentChange;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Resource Attachment Change</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ResourceAttachmentChangeTest extends DiffTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ResourceAttachmentChangeTest.class);
	}

	/**
	 * Constructs a new Resource Attachment Change test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceAttachmentChangeTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Resource Attachment Change test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected ResourceAttachmentChange getFixture() {
		return (ResourceAttachmentChange)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(CompareFactory.eINSTANCE.createResourceAttachmentChange());
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

} //ResourceAttachmentChangeTest
