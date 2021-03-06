package su.katso.synonym.tasks

import android.os.Bundle
import org.koin.core.parameter.parametersOf
import org.koin.standalone.get
import su.katso.synonym.common.app.SynonymActivity
import su.katso.synonym.common.arch.BaseController
import su.katso.synonym.common.arch.ToastCommand
import su.katso.synonym.common.entities.Task.Status
import su.katso.synonym.common.usecases.ChangeTaskStatusUseCase
import su.katso.synonym.common.usecases.ChangeTaskStatusUseCase.Method
import su.katso.synonym.common.usecases.CreateTaskUseCase
import su.katso.synonym.common.usecases.GetTaskListUseCase
import su.katso.synonym.common.utils.getError
import su.katso.synonym.tasks.adapter.TaskViewObject

class TasksController(
    view: TasksView, private val arguments: Bundle = Bundle.EMPTY
) : BaseController<TasksView, TasksModel>(view, TasksModel()) {

    override fun onFirstBind(view: TasksView) {
        getTaskList()

        arguments.getString(SynonymActivity.EXTRA_URI)?.let {
            sendCommand(ToastCommand(it))
        }
    }

    override fun onBind(view: TasksView) {
        bindTo(view.recycleViewItemClicks()) {
            when (val item = it.item) {
                is TaskViewObject -> changeTaskStatus(
                    item.id, if (item.status == Status.PAUSED) Method.RESUME else Method.PAUSE
                )
            }
        }

        bindTo(view.floatingButtonClicks()) {
            createTask("magnet")
        }
    }

    private fun createTask(uri: String) {
        get<CreateTaskUseCase> { parametersOf(uri) }.interact {

            onStart {
                modifyState { isLoading = true }
            }

            onSuccess {
                modifyState {
                    isLoading = false
                    tasks = it.tasks.map(::TaskViewObject)
                }
            }

            onError {
                val error = it.getError()
                error?.let { sendCommand(ToastCommand(it.toString())) }
                    ?: run { sendCommand(ToastCommand(it.message.orEmpty())) }

                modifyState { isLoading = false }
            }
        }
    }

    private fun changeTaskStatus(id: String, method: Method) {
        get<ChangeTaskStatusUseCase> { parametersOf(id, method) }.interact {
            onStart {
                modifyState { isLoading = true }
            }

            onSuccess {
                modifyState {
                    isLoading = false
                    tasks = it.tasks.map(::TaskViewObject)
                }
            }

            onError {
                val error = it.getError()
                error?.let { sendCommand(ToastCommand(it.toString())) }
                    ?: run { sendCommand(ToastCommand(it.message.orEmpty())) }

                modifyState { isLoading = false }
            }
        }
    }

    private fun getTaskList() {
        get<GetTaskListUseCase>().interact {
            onStart {
                modifyState { isLoading = true }
            }

            onNext {
                modifyState {
                    isLoading = false
                    tasks = it.tasks.map(::TaskViewObject)
                }
            }

            onError {
                val error = it.getError()
                error?.let { sendCommand(ToastCommand(it.toString())) }
                    ?: run { sendCommand(ToastCommand(it.message.orEmpty())) }

                modifyState { isLoading = false }
            }
        }
    }
}