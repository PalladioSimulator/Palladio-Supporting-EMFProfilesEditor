# EMF-Profiles-Sirius-Editor
A sirius-based editor for the definition of EMF Profiles. It was created, as the old editor is build on outdated technology, contains issues, and is no longer maintained.

## Using the EMF Profiles Sirius Editor
* Install the graphical editor from the site
* Create a new EMF Profiles (Sirius) project
* Navigate to and open the EMF Profiles diagram
* If the meta models that you want to use within your profile is NOT installed in your Eclipse instance consider
    * starting an inner Eclipse instance (effect: meta models are installed in the inner instance)
    * import your profiles project
    * edit your profile in the inner Eclipse instance
    * when finished: close inner instance and refresh profiles project in outer Eclipse instance (do NOT try to edit profile in outer instance)
At this step, you can import classes using the "Import EClass" tool, create stereotypes, make them extend and reference classes and create tagged values. The "Display referenced and extended EClasses" tool can be used in case a stereotype extends or references a class but the class is not shown in the diagram. This can be useful when dealing with old EMF Profiles. The created profile can be directly used in the same Eclipse instance. Use the "Registered EMF Profiles" view to make sure that the profile has been registered.

### Workaround for missing profile application in edtiors
If applications do not become visible in the editor, please try [this](https://github.com/PalladioSimulator/Palladio-Addons-DataFlowConfidentiality-Analysis/issues/35#issuecomment-1378762708)

## Support
For support
* visit our [issue tracking system](https://palladio-simulator.com/jira)
* contact us via our [mailing list](https://lists.ira.uni-karlsruhe.de/mailman/listinfo/palladio-dev)

For professional support, please fill in our [contact form](http://www.palladio-simulator.com/about_palladio/support/).

