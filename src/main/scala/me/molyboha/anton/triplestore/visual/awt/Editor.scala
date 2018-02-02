package me.molyboha.anton.triplestore.visual.awt

import java.awt.event.{ActionEvent, ActionListener}
import java.awt._

import me.molyboha.anton.triplestore.data.model
import me.molyboha.anton.triplestore.data.model.Notion
import me.molyboha.anton.triplestore.visual.awt.layout.SpringAutoLayout

class Editor(factory: model.Factory[String], startingNotion: Notion[String]) extends Panel(new BorderLayout()){
  class NotionAdder extends Panel {
    val editBox = new TextField(20)
    val button = new Button("New")
    add(editBox)
    add(button)
    button.addActionListener((actionEvent: ActionEvent) => {
      val notion = factory.notion(editBox.getText)
      Editor.this.view.pin(Editor.this.view.center)
      Editor.this.view.center = notion
      Editor.this.view.pin(notion)
    })
  }
  class RelationAdder extends Panel(new GridLayout(2, 4)) {
    val setSubject = new Button("Subject")
    val setVerb = new Button("Verb")
    val setObject = new Button("Object")
    val createButton = new Button("Create")
    val emptyText = "<empty>"
    val subjectLabel = new Label(emptyText)
    val verbLabel = new Label(emptyText)
    val objectLabel = new Label(emptyText)
    val dataText = new TextField()
    createButton.setEnabled(false)

    private var _subject: Option[Notion[String]] = None
    private var _verb: Option[Notion[String]] = None
    private var _obj: Option[Notion[String]] = None
    def subject: Option[Notion[String]] = _subject
    def verb: Option[Notion[String]] = _verb
    def obj: Option[Notion[String]] = _obj
    def subject_=(v: Option[Notion[String]]): Unit = {
      _subject = v
      subjectLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }
    def verb_=(v: Option[Notion[String]]): Unit = {
      _verb = v
      verbLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }
    def obj_=(v: Option[Notion[String]]): Unit = {
      _obj = v
      objectLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }

    setSubject.addActionListener((e: ActionEvent) => {subject = Some(Editor.this.view.center)})
    setVerb.addActionListener((e: ActionEvent) => {verb = Some(Editor.this.view.center)})
    setObject.addActionListener((e: ActionEvent) => {obj = Some(Editor.this.view.center)})
    createButton.addActionListener((e: ActionEvent) => {
      val relName = dataText.getText
      val relData = if( relName.isEmpty ) None else Some(relName)
      val rel = factory.relation(subject.get, verb.get, obj.get, relData)
      subject = None
      verb = None
      obj = None
      Editor.this.view.updateLayout()
    })

    add(setSubject)
    add(setVerb)
    add(setObject)
    add(createButton)
    add(subjectLabel)
    add(verbLabel)
    add(objectLabel)
    add(dataText)
  }

  val view = new CentralView[String](startingNotion, 2, SpringAutoLayout.apply)
  val notionAdder = new NotionAdder
  val relationAdder = new RelationAdder

  val topPanel = new Panel
  topPanel.add(notionAdder)
  topPanel.add(relationAdder)
  add(view)
  add(topPanel, BorderLayout.NORTH)
}