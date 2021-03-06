package doc.examples.school

import java.util.concurrent.atomic.AtomicLong

import org.apache.commons.lang3.StringUtils
import org.mentha.tools.archimate.model.Model
import org.mentha.tools.archimate.model._
import org.mentha.tools.archimate.model.nodes.dsl.Business._
import org.mentha.tools.archimate.model.nodes.dsl.Motivation._
import org.mentha.tools.archimate.model.nodes.dsl._
import org.mentha.tools.archimate.model.view._
import org.mentha.tools.archimate.model.view.dsl._

object MkSchool {

  trait base {
    implicit def model: Model
    implicit def space: Size
  }

  trait learning {
    this: base =>

    private val counter = new AtomicLong()
    private def learningView(title: String): View = {
      val num = StringUtils.leftPad(counter.incrementAndGet().toString, 2, '0')
      model.add(s"v-learning-${num}") { new View() withName { title.replace("#", num) } }
    }

    val $learner = businessRole withName "Learner"

    val $learnerGoal = driver withName "Overall Learner Goal / Driver"
    val $learnerGoalAssoc = $learnerGoal `associated with` $learner

    val $learnerSkillsReq = requirement withName "Specific skills are required"
    val $learnerSkillsReqAssoc = $learnerSkillsReq `associated with` $learner

    val $learnerSkillReqInfluencesLearnerGoal = $learnerSkillsReq `influences` "+" `in` $learnerGoal

    {
      in { learningView("Learning: #. Basics: Learner has a Goal") }
        .add { $learnerGoalAssoc }
        .addNotes { $learnerGoal } { "The Goal (Driver) which motivates the Learner." }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    {
      in { learningView("Learning: #. Basics: Skills are required") }
        .add { $learnerGoalAssoc }.addNotes { $learnerGoal } { "The motivator." }
        .add { $learnerSkillsReqAssoc }.addNotes { $learnerSkillsReq } { "Skills, Knowledge and Experience could help." }
        .add { $learnerSkillReqInfluencesLearnerGoal }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    val $learning = businessProcess withName "Learning"
    val $learnerAssignedToLearning = $learner `assigned to` $learning
    val $learningInfluencesLearnerSkillsReq = $learning `influences` "+" `in` $learnerSkillsReq

    {
      in { learningView("Learning: #. Learning could help") }
        .add { $learnerAssignedToLearning }
        .add { $learnerGoalAssoc }
        .add { $learnerSkillsReqAssoc }
        .add { $learnerSkillReqInfluencesLearnerGoal }
        .add { $learningInfluencesLearnerSkillsReq }
        .addNotes { $learning } { "Learning produces necessary Skills, Knowledge and Experience." }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    val $educationProcess = businessProcess withName "Education"
    val $educationProcessComposesLearning = $educationProcess `composes` $learning
    val $learnerAssignedToEduProcess = $learner `assigned to` $educationProcess

    {
      in { learningView("Learning: #. Education does the Learning") }
        .add { $educationProcessComposesLearning }
        .add { $learnerAssignedToLearning }
        .add { $learnerSkillsReqAssoc }
        .add { $learningInfluencesLearnerSkillsReq }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    val $teacher = businessRole withName "Teacher"
    val $teacherAssignedToEduProcess = $teacher `assigned to` $educationProcess

    {
      in { learningView("Learning: #. Education Overview (1)") }
        .add { $teacherAssignedToEduProcess }
        .add { $learnerAssignedToEduProcess }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    val $teaching = businessProcess withName "Teaching"
    val $educationProcessComposesTeaching = $educationProcess `composes` $teaching
    val $teachingFlowsKnowledgeToLearning = $teaching `flows` "Knowledge" `to` $learning
    val $teacherAssignedToTeaching = $teacher `assigned to` $teaching

    {
      in { learningView("Learning: #. Education Overview (2)") }
        .add { $educationProcessComposesTeaching }
        .add { $educationProcessComposesLearning }
        .add { $teacherAssignedToTeaching }
        .add { $learnerAssignedToLearning }
        .add { $teachingFlowsKnowledgeToLearning }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }

    {
      in { learningView("Learning: #. Education Overview (3)") }
        .add { $teacherAssignedToTeaching }
        .add { $learnerAssignedToLearning }
        .add { $teachingFlowsKnowledgeToLearning }
        .add { $learnerSkillsReqAssoc }
        .add { $learningInfluencesLearnerSkillsReq }
        .placeLikeBefore()
        .resizeNodesToTitle()
        .layout()
    }
  }


}

